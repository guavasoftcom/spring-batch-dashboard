import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import DatabaseIcon from '~/components/DatabaseIcon/DatabaseIcon';

const labelOf = (container: HTMLElement) => container.querySelector('text')?.textContent;
const fillOf = (container: HTMLElement) => container.querySelector('path')?.getAttribute('fill');

describe('DatabaseIcon', () => {
  it.each([
    ['POSTGRESQL', 'PG', '#336791'],
    ['MYSQL', 'MY', '#00618A'],
    ['ORACLE', 'OR', '#C74634'],
  ] as const)('renders the %s palette', (type, label, fill) => {
    const { container } = render(<DatabaseIcon type={type} />);
    expect(labelOf(container)).toBe(label);
    expect(fillOf(container)).toBe(fill);
  });

  it('matches the type case-insensitively', () => {
    const { container } = render(<DatabaseIcon type="postgresql" />);
    expect(labelOf(container)).toBe('PG');
  });

  it('falls back to a neutral palette for an unknown type', () => {
    const { container } = render(<DatabaseIcon type="something-else" />);
    expect(labelOf(container)).toBe('?');
    expect(fillOf(container)).toBe('#64748B');
  });

  it('forwards SvgIconProps such as fontSize', () => {
    const { container } = render(<DatabaseIcon type="ORACLE" fontSize="small" data-testid="db-icon" />);
    const svg = container.querySelector('[data-testid="db-icon"]');
    expect(svg).toBeInTheDocument();
    expect(svg?.tagName.toLowerCase()).toBe('svg');
  });
});

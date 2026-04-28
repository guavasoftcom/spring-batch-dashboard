import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import TilePaper from '~/components/TilePaper/TilePaper';

describe('TilePaper', () => {
  it('renders children inside the paper', () => {
    render(<TilePaper><span>tile body</span></TilePaper>);
    expect(screen.getByText('tile body')).toBeInTheDocument();
  });

  it('renders even when minHeight is omitted', () => {
    const { container } = render(<TilePaper><span>tile body</span></TilePaper>);
    expect(container.firstChild).toBeTruthy();
  });

  it('accepts a minHeight prop', () => {
    const { container } = render(<TilePaper minHeight={400}><span>tile body</span></TilePaper>);
    expect(container.firstChild).toBeTruthy();
  });
});

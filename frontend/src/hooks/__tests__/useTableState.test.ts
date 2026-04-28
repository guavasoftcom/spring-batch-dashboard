import { act, renderHook } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { useTableState } from '~/hooks/useTableState';

describe('useTableState', () => {
  it('seeds with the provided initial sort field and default desc direction', () => {
    const { result } = renderHook(() => useTableState<'name' | 'date'>('name'));

    expect(result.current.sortBy).toBe('name');
    expect(result.current.sortDir).toBe('desc');
    expect(result.current.page).toBe(0);
  });

  it('respects an explicit initial direction', () => {
    const { result } = renderHook(() => useTableState<'name'>('name', 'asc'));
    expect(result.current.sortDir).toBe('asc');
  });

  it('toggles direction when the same field is clicked again', () => {
    const { result } = renderHook(() => useTableState<'name'>('name', 'desc'));

    act(() => result.current.onSortChange('name'));
    expect(result.current.sortDir).toBe('asc');
    act(() => result.current.onSortChange('name'));
    expect(result.current.sortDir).toBe('desc');
  });

  it('switches field and resets direction to desc when a different field is clicked', () => {
    const { result } = renderHook(() => useTableState<'name' | 'date'>('name', 'asc'));

    act(() => result.current.onSortChange('date'));

    expect(result.current.sortBy).toBe('date');
    expect(result.current.sortDir).toBe('desc');
  });

  it('resets page to 0 on any sort change', () => {
    const { result } = renderHook(() => useTableState<'name' | 'date'>('name'));

    act(() => result.current.setPage(5));
    expect(result.current.page).toBe(5);

    act(() => result.current.onSortChange('date'));
    expect(result.current.page).toBe(0);
  });

  it('exposes setPage as a controlled setter', () => {
    const { result } = renderHook(() => useTableState<'name'>('name'));

    act(() => result.current.setPage(3));
    expect(result.current.page).toBe(3);
  });
});

import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  NAV_WIDTH_DEFAULT,
  NAV_WIDTH_MAX,
  NAV_WIDTH_MIN,
  NAV_WIDTH_STORAGE_KEY,
  clampNavWidth,
  useNavResize,
} from '../useNavResize';

const pointerEvent = (clientX: number) => {
  const target = { setPointerCapture: vi.fn(), releasePointerCapture: vi.fn() };
  return {
    event: { clientX, pointerId: 1, preventDefault: vi.fn(), currentTarget: target } as never,
    target,
  };
};

const keyEvent = (key: string, shiftKey = false) => {
  const preventDefault = vi.fn();
  return { event: { key, shiftKey, preventDefault } as never, preventDefault };
};

describe('useNavResize', () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  it('defaults to 240px with nothing stored', () => {
    const { result } = renderHook(() => useNavResize());
    expect(result.current.width).toBe(NAV_WIDTH_DEFAULT);
    expect(result.current.resizing).toBe(false);
  });

  it('restores a stored width, clamped to the allowed range', () => {
    window.localStorage.setItem(NAV_WIDTH_STORAGE_KEY, '320');
    expect(renderHook(() => useNavResize()).result.current.width).toBe(320);

    window.localStorage.setItem(NAV_WIDTH_STORAGE_KEY, '9999');
    expect(renderHook(() => useNavResize()).result.current.width).toBe(NAV_WIDTH_MAX);

    window.localStorage.setItem(NAV_WIDTH_STORAGE_KEY, '10');
    expect(renderHook(() => useNavResize()).result.current.width).toBe(NAV_WIDTH_MIN);
  });

  it('falls back to the default when the stored value is not a number', () => {
    window.localStorage.setItem(NAV_WIDTH_STORAGE_KEY, 'wide');
    expect(renderHook(() => useNavResize()).result.current.width).toBe(NAV_WIDTH_DEFAULT);
  });

  it('falls back to the default when storage throws', () => {
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('blocked');
    });
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('blocked');
    });
    const { result } = renderHook(() => useNavResize());
    expect(result.current.width).toBe(NAV_WIDTH_DEFAULT);

    act(() => result.current.handleProps.onDoubleClick());
    expect(result.current.width).toBe(NAV_WIDTH_DEFAULT);
  });

  it('follows the pointer while dragging and persists only once the drag ends', () => {
    const { result } = renderHook(() => useNavResize());
    const down = pointerEvent(240);

    act(() => result.current.handleProps.onPointerDown(down.event));
    expect(result.current.resizing).toBe(true);
    expect(down.target.setPointerCapture).toHaveBeenCalledWith(1);

    act(() => result.current.handleProps.onPointerMove(pointerEvent(300).event));
    expect(result.current.width).toBe(300);
    expect(window.localStorage.getItem(NAV_WIDTH_STORAGE_KEY)).toBe(String(NAV_WIDTH_DEFAULT));

    const up = pointerEvent(300);
    act(() => result.current.handleProps.onPointerUp(up.event));
    expect(result.current.resizing).toBe(false);
    expect(up.target.releasePointerCapture).toHaveBeenCalledWith(1);
    expect(window.localStorage.getItem(NAV_WIDTH_STORAGE_KEY)).toBe('300');
  });

  it('clamps dragging to the min and max widths', () => {
    const { result } = renderHook(() => useNavResize());
    act(() => result.current.handleProps.onPointerDown(pointerEvent(240).event));

    act(() => result.current.handleProps.onPointerMove(pointerEvent(5000).event));
    expect(result.current.width).toBe(NAV_WIDTH_MAX);

    act(() => result.current.handleProps.onPointerMove(pointerEvent(-5000).event));
    expect(result.current.width).toBe(NAV_WIDTH_MIN);
  });

  it('ignores pointer moves and ups when no drag is in progress', () => {
    const { result } = renderHook(() => useNavResize());
    const up = pointerEvent(100);

    act(() => result.current.handleProps.onPointerMove(pointerEvent(400).event));
    act(() => result.current.handleProps.onPointerUp(up.event));

    expect(result.current.width).toBe(NAV_WIDTH_DEFAULT);
    expect(up.target.releasePointerCapture).not.toHaveBeenCalled();
  });

  it('ends the drag on pointer cancel', () => {
    const { result } = renderHook(() => useNavResize());
    act(() => result.current.handleProps.onPointerDown(pointerEvent(240).event));
    act(() => result.current.handleProps.onPointerCancel(pointerEvent(240).event));
    expect(result.current.resizing).toBe(false);
  });

  it('resizes with the arrow keys (16px, or 48px with Shift)', () => {
    const { result } = renderHook(() => useNavResize());

    const right = keyEvent('ArrowRight');
    act(() => result.current.handleProps.onKeyDown(right.event));
    expect(result.current.width).toBe(NAV_WIDTH_DEFAULT + 16);
    expect(right.preventDefault).toHaveBeenCalled();

    act(() => result.current.handleProps.onKeyDown(keyEvent('ArrowLeft', true).event));
    expect(result.current.width).toBe(NAV_WIDTH_DEFAULT + 16 - 48);
  });

  it('jumps to the min and max with Home and End', () => {
    const { result } = renderHook(() => useNavResize());

    act(() => result.current.handleProps.onKeyDown(keyEvent('End').event));
    expect(result.current.width).toBe(NAV_WIDTH_MAX);

    act(() => result.current.handleProps.onKeyDown(keyEvent('Home').event));
    expect(result.current.width).toBe(NAV_WIDTH_MIN);
  });

  it('leaves unrelated keys alone', () => {
    const { result } = renderHook(() => useNavResize());
    const tab = keyEvent('Tab');

    act(() => result.current.handleProps.onKeyDown(tab.event));

    expect(result.current.width).toBe(NAV_WIDTH_DEFAULT);
    expect(tab.preventDefault).not.toHaveBeenCalled();
  });

  it('persists keyboard changes and resets to the default on double click', () => {
    const { result } = renderHook(() => useNavResize());

    act(() => result.current.handleProps.onKeyDown(keyEvent('End').event));
    expect(window.localStorage.getItem(NAV_WIDTH_STORAGE_KEY)).toBe(String(NAV_WIDTH_MAX));

    act(() => result.current.handleProps.onDoubleClick());
    expect(result.current.width).toBe(NAV_WIDTH_DEFAULT);
    expect(window.localStorage.getItem(NAV_WIDTH_STORAGE_KEY)).toBe(String(NAV_WIDTH_DEFAULT));
  });
});

describe('clampNavWidth', () => {
  it('keeps values inside [min, max]', () => {
    expect(clampNavWidth(0)).toBe(NAV_WIDTH_MIN);
    expect(clampNavWidth(300)).toBe(300);
    expect(clampNavWidth(1000)).toBe(NAV_WIDTH_MAX);
  });
});

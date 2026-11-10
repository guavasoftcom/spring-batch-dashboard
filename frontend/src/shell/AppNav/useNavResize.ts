import { useCallback, useEffect, useRef, useState } from 'react';
import type { KeyboardEvent, PointerEvent } from 'react';

export const NAV_WIDTH_STORAGE_KEY = 'spring-batch-dashboard.nav-width';
export const NAV_WIDTH_DEFAULT = 240;
export const NAV_WIDTH_MIN = 200;
export const NAV_WIDTH_MAX = 480;

const KEY_STEP = 16;
const KEY_STEP_LARGE = 48;

export type NavResizeHandleProps = {
  onPointerDown: (event: PointerEvent<HTMLElement>) => void;
  onPointerMove: (event: PointerEvent<HTMLElement>) => void;
  onPointerUp: (event: PointerEvent<HTMLElement>) => void;
  onPointerCancel: (event: PointerEvent<HTMLElement>) => void;
  onKeyDown: (event: KeyboardEvent<HTMLElement>) => void;
  onDoubleClick: () => void;
};

export const clampNavWidth = (value: number) =>
  Math.min(NAV_WIDTH_MAX, Math.max(NAV_WIDTH_MIN, value));

const readStoredWidth = (): number => {
  try {
    const stored = window.localStorage.getItem(NAV_WIDTH_STORAGE_KEY);
    const parsed = stored != null ? Number(stored) : Number.NaN;
    return Number.isFinite(parsed) ? clampNavWidth(parsed) : NAV_WIDTH_DEFAULT;
  } catch {
    return NAV_WIDTH_DEFAULT;
  }
};

/**
 * Width state for the resizable side nav: drag (pointer), keyboard and double-click-to-reset
 * handlers for the resize handle, with the width persisted to localStorage once a drag ends.
 */
export const useNavResize = () => {
  const [width, setWidth] = useState<number>(readStoredWidth);
  const [resizing, setResizing] = useState(false);
  const dragStart = useRef<{ pointerX: number; startWidth: number } | null>(null);

  useEffect(() => {
    if (resizing) {
      return;
    }
    try {
      window.localStorage.setItem(NAV_WIDTH_STORAGE_KEY, String(width));
    } catch {
      // storage unavailable (private mode, blocked) — the width just won't persist
    }
  }, [width, resizing]);

  const onPointerDown = useCallback(
    (event: PointerEvent<HTMLElement>) => {
      event.preventDefault();
      event.currentTarget.setPointerCapture?.(event.pointerId);
      dragStart.current = { pointerX: event.clientX, startWidth: width };
      setResizing(true);
    },
    [width],
  );

  const onPointerMove = useCallback((event: PointerEvent<HTMLElement>) => {
    const start = dragStart.current;
    if (!start) {
      return;
    }
    setWidth(clampNavWidth(start.startWidth + event.clientX - start.pointerX));
  }, []);

  const endDrag = useCallback((event: PointerEvent<HTMLElement>) => {
    if (!dragStart.current) {
      return;
    }
    event.currentTarget.releasePointerCapture?.(event.pointerId);
    dragStart.current = null;
    setResizing(false);
  }, []);

  const onKeyDown = useCallback((event: KeyboardEvent<HTMLElement>) => {
    const step = event.shiftKey ? KEY_STEP_LARGE : KEY_STEP;
    switch (event.key) {
      case 'ArrowLeft':
        setWidth((current) => clampNavWidth(current - step));
        break;
      case 'ArrowRight':
        setWidth((current) => clampNavWidth(current + step));
        break;
      case 'Home':
        setWidth(NAV_WIDTH_MIN);
        break;
      case 'End':
        setWidth(NAV_WIDTH_MAX);
        break;
      default:
        return;
    }
    event.preventDefault();
  }, []);

  const onDoubleClick = useCallback(() => setWidth(NAV_WIDTH_DEFAULT), []);

  const handleProps: NavResizeHandleProps = {
    onPointerDown,
    onPointerMove,
    onPointerUp: endDrag,
    onPointerCancel: endDrag,
    onKeyDown,
    onDoubleClick,
  };

  return { width, resizing, handleProps };
};

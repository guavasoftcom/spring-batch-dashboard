import { useState } from 'react';

export type SortDirection = 'asc' | 'desc';

export type TableState<F extends string> = {
  sortBy: F;
  sortDir: SortDirection;
  page: number;
  setPage: (page: number) => void;
  onSortChange: (field: F) => void;
};

/**
 * Standard sort + pagination state for tile tables.
 * Toggles direction when the same column is clicked, otherwise
 * switches to the new column at `desc` and resets to page 0.
 */
export const useTableState = <F extends string>(
  initialSortBy: F,
  initialSortDir: SortDirection = 'desc',
): TableState<F> => {
  const [sortBy, setSortBy] = useState<F>(initialSortBy);
  const [sortDir, setSortDir] = useState<SortDirection>(initialSortDir);
  const [page, setPage] = useState(0);

  const onSortChange = (field: F) => {
    setPage(0);
    if (field === sortBy) {
      setSortDir((dir) => (dir === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDir('desc');
    }
  };

  return { sortBy, sortDir, page, setPage, onSortChange };
};

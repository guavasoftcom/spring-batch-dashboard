import { useCallback, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getJobLastRuns } from '~/api';
import { useEnvQuery } from '~/hooks';
import { useWindow } from '~/shell/WindowContext';
import {
  buildJobRows,
  countByFilter,
  filterAndSortRows,
  type JobFilter,
  type JobSort,
} from '../../jobInsights';
import { useJobDurationTrends } from '../../useJobDurationTrends';
import JobLastRunsTile from './JobLastRunsTile';

const JobLastRunsTileContainer = () => {
  const navigate = useNavigate();
  const { windowDays } = useWindow();
  const lastRuns = useEnvQuery(['job-last-runs', windowDays], () => getJobLastRuns(windowDays));
  // Same cached query the trends feed uses elsewhere; supplies the sparkline and "usual" duration.
  const trends = useJobDurationTrends();

  const [filter, setFilter] = useState<JobFilter>('all');
  const [query, setQuery] = useState('');
  const [sort, setSort] = useState<JobSort>('attention');
  const [expanded, setExpanded] = useState<ReadonlySet<string>>(new Set());

  const rows = useMemo(
    () => (lastRuns.data ? buildJobRows(lastRuns.data, trends.data ?? []) : null),
    [lastRuns.data, trends.data],
  );
  const counts = useMemo(() => countByFilter(rows ?? []), [rows]);
  const visibleRows = useMemo(
    () => filterAndSortRows(rows ?? [], filter, query, sort),
    [rows, filter, query, sort],
  );

  const toggleExpanded = useCallback((jobName: string) => {
    setExpanded((current) => {
      const next = new Set(current);
      if (!next.delete(jobName)) {
        next.add(jobName);
      }
      return next;
    });
  }, []);

  return (
    <JobLastRunsTile
      rows={rows}
      visibleRows={visibleRows}
      counts={counts}
      loading={lastRuns.loading || trends.loading}
      error={lastRuns.error ?? trends.error}
      windowDays={windowDays}
      filter={filter}
      query={query}
      sort={sort}
      expanded={expanded}
      onFilterChange={setFilter}
      onQueryChange={setQuery}
      onSortChange={setSort}
      onToggleExpanded={toggleExpanded}
      onJobClick={(jobName) => navigate(`/jobs/${encodeURIComponent(jobName)}`)}
      onRunClick={(jobName, executionId) =>
        navigate(`/jobs/${encodeURIComponent(jobName)}/executions/${executionId}`)
      }
    />
  );
};

export default JobLastRunsTileContainer;

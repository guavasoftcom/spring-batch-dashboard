import { useCallback, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getEnvironments } from '~/api';
import { useEnvironment } from '~/shell/EnvironmentContext';
import EnvironmentSelector from './EnvironmentSelector';

const EnvironmentSelectorContainer = () => {
  const navigate = useNavigate();
  const { environment, setEnvironment } = useEnvironment();

  const { data, isPending } = useQuery({
    queryKey: ['environments'],
    queryFn: getEnvironments,
  });

  const options = useMemo(() => data ?? [], [data]);

  useEffect(() => {
    if (options.length > 0 && !options.includes(environment)) {
      setEnvironment(options[0]);
    }
  }, [options, environment, setEnvironment]);

  const handleChange = useCallback((next: string) => {
    if (next === environment) { 
      return;
    }
    setEnvironment(next);
    navigate('/overview');
  }, [environment, setEnvironment, navigate]);

  if (!isPending && options.length <= 1) {
    return null;
  }

  return (
    <EnvironmentSelector
      value={environment}
      options={options}
      onChange={handleChange}
      loading={isPending}
    />
  );
};

export default EnvironmentSelectorContainer;

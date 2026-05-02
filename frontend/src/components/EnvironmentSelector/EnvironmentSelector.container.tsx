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
  const optionNames = useMemo(() => options.map((env) => env.name), [options]);
  const selectedType = useMemo(
    () => options.find((env) => env.name === environment)?.type ?? '',
    [options, environment],
  );
  const validValue = optionNames.includes(environment) ? environment : '';

  useEffect(() => {
    if (optionNames.length > 0 && !optionNames.includes(environment)) {
      setEnvironment(optionNames[0]);
    }
  }, [optionNames, environment, setEnvironment]);

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
      value={validValue}
      selectedType={selectedType}
      options={options}
      onChange={handleChange}
      loading={isPending}
    />
  );
};

export default EnvironmentSelectorContainer;

import { createContext, useContext } from 'react';

type EnvironmentContextValue = {
  environment: string;
  setEnvironment: (value: string) => void;
};

export const EnvironmentContext = createContext<EnvironmentContextValue>({
  environment: '',
  setEnvironment: () => {},
});

export const useEnvironment = () => useContext(EnvironmentContext);

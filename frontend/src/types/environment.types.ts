export type DatabaseEngine = 'POSTGRESQL' | 'MYSQL' | 'ORACLE' | 'SQLSERVER';

export type EnvironmentInfo = {
  name: string;
  type: DatabaseEngine;
};

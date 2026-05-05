import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, logout } from '~/api';
import { useColorMode } from '~/theme';
import type { CurrentUserResponse } from '~/types';
import { useNav } from '../NavContext';
import AppHeader from './AppHeader';

const AppHeaderContainer = () => {
  const navigate = useNavigate();
  const { mode } = useColorMode();
  const { navOpen, setNavOpen } = useNav();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const { data: user = null } = useQuery<CurrentUserResponse | null>({
    queryKey: ['current-user'],
    queryFn: () => getCurrentUser().catch(() => null),
    staleTime: 5 * 60_000,
  });
  const displayName = user?.name?.trim() || user?.login || '';

  const handleLogout = async () => {
    try {
      setIsLoggingOut(true);
      await logout();
    } finally {
      navigate('/', { replace: true });
      setIsLoggingOut(false);
    }
  };

  return (
    <AppHeader
      user={user}
      displayName={displayName}
      mode={mode}
      isLoggingOut={isLoggingOut}
      onToggleNav={() => setNavOpen(!navOpen)}
      onTitleClick={() => navigate('/overview')}
      onLogout={handleLogout}
    />
  );
};

export default AppHeaderContainer;

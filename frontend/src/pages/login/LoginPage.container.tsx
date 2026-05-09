import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, getOAuth2Providers } from '~/api';
import type { OAuth2Provider } from '~/types';
import LoginPage from './LoginPage';

const LoginPageContainer = () => {
  const navigate = useNavigate();
  const [checking, setChecking] = useState(true);
  const [providers, setProviders] = useState<OAuth2Provider[]>([]);

  useEffect(() => {
    let active = true;

    getCurrentUser()
      .then(() => {
        if (!active) {return;}
        navigate('/overview', { replace: true });
      })
      .catch(() => {
        if (!active) {return;}
        getOAuth2Providers()
          .then((list) => {
            if (active) {
              setProviders(list);
            }
          })
          .catch(() => {
            // Leave providers empty; LoginPage renders an inline error/empty state.
          })
          .finally(() => {
            if (active) {
              setChecking(false);
            }
          });
      });

    return () => {
      active = false;
    };
  }, [navigate]);

  return <LoginPage checking={checking} providers={providers} />;
};

export default LoginPageContainer;

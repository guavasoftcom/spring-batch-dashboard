import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser } from '~/api';
import { LOGIN_URL } from '~/config/env';
import LoginPage from './LoginPage';

const LoginPageContainer = () => {
  const navigate = useNavigate();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    let active = true;

    getCurrentUser()
      .then(() => {
        if (!active) {return;}
        navigate('/overview', { replace: true });
      })
      .catch(() => {
        if (active) {
          setChecking(false);
        }
      });

    return () => {
      active = false;
    };
  }, [navigate]);

  return <LoginPage checking={checking} loginUrl={LOGIN_URL} />;
};

export default LoginPageContainer;

import { createContext, useContext } from 'react';

type NavContextValue = {
  navOpen: boolean;
  setNavOpen: (open: boolean) => void;
};

export const NavContext = createContext<NavContextValue>({
  navOpen: true,
  setNavOpen: () => {},
});

export const useNav = () => useContext(NavContext);

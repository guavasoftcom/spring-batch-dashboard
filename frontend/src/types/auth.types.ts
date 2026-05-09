export type CurrentUserResponse = {
  login: string | null;
  name: string | null;
  avatarUrl: string | null;
};

export type OAuth2Provider = {
  id: string;
  label: string;
  loginUrl: string;
  color: string | null;
  /** URL the login button uses as its icon `src`. May be an absolute http(s) URL or a data URI. */
  iconUrl: string | null;
};

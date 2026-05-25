import apiClient from './client';

export interface UserProfile {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
}

export const authApi = {
  getCurrentUser: async (): Promise<UserProfile> => {
    const response = await apiClient.get('/auth/me');
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout');
  },

  loginWithGoogle: () => {
    window.location.href = '/api/oauth2/authorization/google';
  },

  loginWithGitHub: () => {
    window.location.href = '/api/oauth2/authorization/github';
  },
};

import apiClient from './client';

export interface TaskListDTO {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateListRequest {
  title: string;
}

export interface UpdateListRequest {
  title: string;
}

export const listApi = {
  getLists: async (): Promise<TaskListDTO[]> => {
    const response = await apiClient.get('/lists');
    return response.data;
  },

  createList: async (data: CreateListRequest): Promise<TaskListDTO> => {
    const response = await apiClient.post('/lists', data);
    return response.data;
  },

  updateList: async (id: string, data: UpdateListRequest): Promise<TaskListDTO> => {
    const response = await apiClient.put(`/lists/${id}`, data);
    return response.data;
  },

  deleteList: async (id: string): Promise<void> => {
    await apiClient.delete(`/lists/${id}`);
  },
};

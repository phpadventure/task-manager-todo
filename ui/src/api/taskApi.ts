import apiClient from './client';

export type TaskStatus = 'TO_DO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type DueDateFilter = 'overdue' | 'today' | 'next_7_days' | 'all';

export interface TaskDTO {
  id: string;
  listId: string;
  userId: string;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  dueDate?: string;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  dueDate?: string;
}

export interface TaskFilters {
  status?: TaskStatus;
  priority?: TaskPriority;
  dueDateFilter?: DueDateFilter;
}

export const taskApi = {
  getTasksByList: async (listId: string, filters?: TaskFilters): Promise<TaskDTO[]> => {
    const response = await apiClient.get(`/lists/${listId}/tasks`, {
      params: filters,
    });
    return response.data;
  },

  getTasks: async (filters?: TaskFilters): Promise<TaskDTO[]> => {
    const response = await apiClient.get('/tasks', {
      params: filters,
    });
    return response.data;
  },

  searchTasks: async (query: string): Promise<TaskDTO[]> => {
    const response = await apiClient.get('/tasks/search', {
      params: { q: query },
    });
    return response.data;
  },

  getOverdueTasks: async (): Promise<TaskDTO[]> => {
    const response = await apiClient.get('/tasks/overdue');
    return response.data;
  },

  getDueSoonTasks: async (): Promise<TaskDTO[]> => {
    const response = await apiClient.get('/tasks/due-soon');
    return response.data;
  },

  createTask: async (listId: string, data: CreateTaskRequest): Promise<TaskDTO> => {
    const response = await apiClient.post(`/lists/${listId}/tasks`, data);
    return response.data;
  },

  updateTask: async (listId: string, taskId: string, data: UpdateTaskRequest): Promise<TaskDTO> => {
    const response = await apiClient.put(`/lists/${listId}/tasks/${taskId}`, data);
    return response.data;
  },

  updateTaskStatus: async (taskId: string, status: TaskStatus): Promise<TaskDTO> => {
    const response = await apiClient.patch(`/tasks/${taskId}/status`, null, {
      params: { status },
    });
    return response.data;
  },

  deleteTask: async (listId: string, taskId: string): Promise<void> => {
    await apiClient.delete(`/lists/${listId}/tasks/${taskId}`);
  },
};

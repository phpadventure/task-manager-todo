import type { TaskStatus, TaskPriority, DueDateFilter } from '@/api/taskApi';

export interface FilterState {
  status?: TaskStatus[];
  priority?: TaskPriority[];
  dueDateFilter?: DueDateFilter;
  searchQuery?: string;
}

export const getDefaultFilters = (): FilterState => ({
  dueDateFilter: 'all',
});

export const buildQueryParams = (filters: FilterState): Record<string, any> => {
  const params: Record<string, any> = {};

  if (filters.status?.length === 1) {
    params.status = filters.status[0];
  }
  if (filters.priority?.length === 1) {
    params.priority = filters.priority[0];
  }
  if (filters.dueDateFilter && filters.dueDateFilter !== 'all') {
    params.dueDateFilter = filters.dueDateFilter;
  }

  return params;
};

export const validateFilters = (filters: FilterState): boolean => {
  const validStatuses: TaskStatus[] = ['TO_DO', 'IN_PROGRESS', 'DONE'];
  const validPriorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];
  const validDateFilters: DueDateFilter[] = ['overdue', 'today', 'next_7_days', 'all'];

  if (filters.status) {
    if (!Array.isArray(filters.status)) return false;
    if (!filters.status.every((s) => validStatuses.includes(s))) return false;
  }

  if (filters.priority) {
    if (!Array.isArray(filters.priority)) return false;
    if (!filters.priority.every((p) => validPriorities.includes(p))) return false;
  }

  if (filters.dueDateFilter) {
    if (!validDateFilters.includes(filters.dueDateFilter)) return false;
  }

  return true;
};

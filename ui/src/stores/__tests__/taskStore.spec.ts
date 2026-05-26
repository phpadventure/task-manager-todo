import { describe, it, expect, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useTaskStore } from '../taskStore';

describe('taskStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('initializes with empty tasks', () => {
    const store = useTaskStore();
    expect(store.tasks).toEqual([]);
    expect(store.loading).toBe(false);
    expect(store.error).toBeNull();
  });

  it('sets tasks', () => {
    const store = useTaskStore();
    const mockTasks = [
      {
        id: '1',
        title: 'Task 1',
        description: 'Description 1',
        status: 'TO_DO' as const,
        priority: 'MEDIUM' as const,
        dueDate: '2026-05-26',
        listId: 'list-1',
      },
    ];

    store.setTasks(mockTasks);
    expect(store.tasks).toEqual(mockTasks);
  });

  it('adds a task', () => {
    const store = useTaskStore();
    const newTask = {
      id: '1',
      title: 'New Task',
      status: 'TO_DO' as const,
      priority: 'HIGH' as const,
      listId: 'list-1',
    };

    store.addTask(newTask);
    expect(store.tasks).toContainEqual(newTask);
    expect(store.tasks.length).toBe(1);
  });

  it('updates a task', () => {
    const store = useTaskStore();
    const task = {
      id: '1',
      title: 'Original',
      status: 'TO_DO' as const,
      priority: 'MEDIUM' as const,
      listId: 'list-1',
    };

    store.addTask(task);
    const updated = { ...task, title: 'Updated' };
    store.updateTask('1', updated);

    expect(store.tasks[0].title).toBe('Updated');
  });

  it('deletes a task', () => {
    const store = useTaskStore();
    const task = {
      id: '1',
      title: 'Task to delete',
      status: 'TO_DO' as const,
      priority: 'MEDIUM' as const,
      listId: 'list-1',
    };

    store.addTask(task);
    expect(store.tasks.length).toBe(1);

    store.deleteTask('1');
    expect(store.tasks.length).toBe(0);
  });

  it('filters tasks by status', () => {
    const store = useTaskStore();
    const tasks = [
      {
        id: '1',
        title: 'Todo task',
        status: 'TO_DO' as const,
        priority: 'MEDIUM' as const,
        listId: 'list-1',
      },
      {
        id: '2',
        title: 'Done task',
        status: 'DONE' as const,
        priority: 'MEDIUM' as const,
        listId: 'list-1',
      },
    ];

    store.setTasks(tasks);
    store.setFilters({ status: ['TO_DO'] });

    const filtered = store.getTasksByListId('list-1');
    expect(filtered.length).toBe(1);
    expect(filtered[0].status).toBe('TO_DO');
  });

  it('filters tasks by priority', () => {
    const store = useTaskStore();
    const tasks = [
      {
        id: '1',
        title: 'High priority',
        status: 'TO_DO' as const,
        priority: 'HIGH' as const,
        listId: 'list-1',
      },
      {
        id: '2',
        title: 'Low priority',
        status: 'TO_DO' as const,
        priority: 'LOW' as const,
        listId: 'list-1',
      },
    ];

    store.setTasks(tasks);
    store.setFilters({ priority: ['HIGH'] });

    const filtered = store.getTasksByListId('list-1');
    expect(filtered.length).toBe(1);
    expect(filtered[0].priority).toBe('HIGH');
  });

  it('searches tasks by title', () => {
    const store = useTaskStore();
    const tasks = [
      {
        id: '1',
        title: 'Buy groceries',
        status: 'TO_DO' as const,
        priority: 'MEDIUM' as const,
        listId: 'list-1',
      },
      {
        id: '2',
        title: 'Fix bug',
        status: 'TO_DO' as const,
        priority: 'MEDIUM' as const,
        listId: 'list-1',
      },
    ];

    store.setTasks(tasks);
    store.setFilters({ searchQuery: 'groceries' });

    const filtered = store.getTasksByListId('list-1');
    expect(filtered.length).toBe(1);
    expect(filtered[0].title).toContain('groceries');
  });

  it('clears filters', () => {
    const store = useTaskStore();
    store.setFilters({ status: ['TO_DO'], priority: ['HIGH'] });
    expect(store.filters.status.length).toBeGreaterThan(0);

    store.clearFilters();
    expect(store.filters.status).toEqual([]);
    expect(store.filters.priority).toEqual([]);
    expect(store.filters.searchQuery).toBe('');
  });

  it('manages loading state', () => {
    const store = useTaskStore();
    expect(store.loading).toBe(false);

    store.loading = true;
    expect(store.loading).toBe(true);
  });
});

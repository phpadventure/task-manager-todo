import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { useListStore } from '@/stores/listStore';
import { useTaskStore } from '@/stores/taskStore';
import TaskList from '../TaskList.vue';

vi.mock('@/composables/useTasks', () => {
  const { ref } = require('vue');
  return {
    useTasks: () => ({
      isLoading: ref(false),
    }),
  };
});

vi.mock('@/utils/dateUtils', () => ({
  formatDate: (date: string) => new Date(date).toLocaleDateString(),
  getDueDateLabel: (date: string) => {
    const today = new Date().toISOString().split('T')[0];
    if (date === today) return 'Today';
    if (date < today) return 'Overdue';
    return date;
  },
  isOverdue: (date: string) => {
    const today = new Date().toISOString().split('T')[0];
    return date < today;
  },
}));

describe('TaskList', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders task list header', () => {
    const wrapper = mount(TaskList);
    expect(wrapper.find('h2').text()).toBe('Tasks');
  });

  it('renders create task button', () => {
    const wrapper = mount(TaskList);
    const button = wrapper.findAll('button').find((btn) => btn.text().includes('Create Task'));
    expect(button?.exists()).toBe(true);
  });

  it('shows empty state when no list selected', () => {
    const wrapper = mount(TaskList);
    expect(wrapper.text()).toContain('Select a list to view tasks');
  });

  it('shows no tasks message when list is selected but empty', () => {
    const listStore = useListStore();
    listStore.setLists([{ id: '1', title: 'Test List', createdAt: new Date().toISOString() }]);
    listStore.setSelectedList('1');

    const wrapper = mount(TaskList);
    expect(wrapper.text()).toContain('No tasks yet');
  });

  it('renders table when tasks exist', () => {
    const listStore = useListStore();
    const taskStore = useTaskStore();

    listStore.setLists([{ id: '1', title: 'Test List', createdAt: new Date().toISOString() }]);
    listStore.setSelectedList('1');

    taskStore.setTasks([
      {
        id: '1',
        listId: '1',
        title: 'Test Task',
        description: 'Test description',
        status: 'TO_DO',
        priority: 'HIGH',
        dueDate: '2026-05-30',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ]);

    const wrapper = mount(TaskList);
    const table = wrapper.find('table');
    expect(table.exists()).toBe(true);
  });

  it('renders badges for status and priority', () => {
    const listStore = useListStore();
    const taskStore = useTaskStore();

    listStore.setLists([{ id: '1', title: 'Test List', createdAt: new Date().toISOString() }]);
    listStore.setSelectedList('1');

    taskStore.setTasks([
      {
        id: '1',
        listId: '1',
        title: 'Test Task',
        description: '',
        status: 'TO_DO',
        priority: 'HIGH',
        dueDate: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      },
    ]);

    const wrapper = mount(TaskList);
    const badges = wrapper.findAll('[class*="badge"]');
    expect(badges.length).toBeGreaterThan(0);
  });
});

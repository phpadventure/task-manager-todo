import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import CreateTaskForm from '../CreateTaskForm.vue';

vi.mock('@/composables/useTasks', () => {
  const { ref } = require('vue');
  return {
    useTasks: () => ({
      createTask: vi.fn(),
      isCreating: ref(false),
    }),
  };
});

vi.mock('@/utils/validation', () => ({
  validateTaskTitle: (title: string) => (!title || !title.trim() ? 'Task title is required' : null),
  validateTaskDescription: (desc: string) =>
    desc && desc.length > 5000 ? 'Description too long' : null,
  validateDueDate: (date: string) => null,
}));

describe('CreateTaskForm', () => {
  let wrapper: any;

  beforeEach(() => {
    wrapper = mount(CreateTaskForm, {
      props: {
        isOpen: true,
        listId: 'list-123',
      },
    });
  });

  it('renders the form when isOpen is true', () => {
    expect(wrapper.find('h2').text()).toBe('Create New Task');
  });

  it('does not render form when isOpen is false', () => {
    wrapper = mount(CreateTaskForm, {
      props: {
        isOpen: false,
        listId: 'list-123',
      },
    });
    expect(wrapper.find('h2').exists()).toBe(false);
  });

  it('renders title input', () => {
    const input = wrapper.find('input[type="text"]');
    expect(input.exists()).toBe(true);
  });

  it('renders description textarea', () => {
    const textarea = wrapper.find('textarea');
    expect(textarea.exists()).toBe(true);
  });

  it('renders due date input', () => {
    const input = wrapper.find('input[type="date"]');
    expect(input.exists()).toBe(true);
  });

  it('renders priority select', () => {
    const select = wrapper.findAll('select')[0];
    expect(select.exists()).toBe(true);
    expect(select.text()).toContain('Low');
    expect(select.text()).toContain('Medium');
    expect(select.text()).toContain('High');
  });

  it('renders cancel and create buttons', () => {
    const buttons = wrapper.findAll('button');
    expect(buttons.length).toBeGreaterThanOrEqual(2);
  });

  it('submit button is disabled when title is empty', () => {
    const submitBtn = wrapper.findAll('button').find((btn) => btn.text().includes('Create Task'));
    expect(submitBtn?.attributes('disabled')).toBeDefined();
  });

  it('submit button is enabled when title is filled', async () => {
    const input = wrapper.find('input[type="text"]');
    await input.setValue('Test Task');
    const submitBtn = wrapper.findAll('button').find((btn) => btn.text().includes('Create Task'));
    expect(submitBtn?.attributes('disabled')).toBeUndefined();
  });

  it('emits close event when cancel clicked', async () => {
    const cancelBtn = wrapper.findAll('button').find((btn) => btn.text().includes('Cancel'));
    await cancelBtn?.trigger('click');
    expect(wrapper.emitted('close')).toBeTruthy();
  });
});

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import TaskFilterBar from '../TaskFilterBar.vue';

describe('TaskFilterBar', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders search input', () => {
    const wrapper = mount(TaskFilterBar);
    const input = wrapper.find('input[type="text"]');
    expect(input.exists()).toBe(true);
    expect(input.attributes('placeholder')).toBe('Search tasks...');
  });

  it('renders status checkboxes', () => {
    const wrapper = mount(TaskFilterBar);
    const labels = wrapper.findAll('label');
    const statusLabels = labels.filter(
      (l) => l.text().includes('To Do') || l.text().includes('In Progress')
    );
    expect(statusLabels.length).toBeGreaterThan(0);
  });

  it('renders priority checkboxes', () => {
    const wrapper = mount(TaskFilterBar);
    const labels = wrapper.findAll('label');
    const priorityLabels = labels.filter(
      (l) => l.text().includes('Low') || l.text().includes('High')
    );
    expect(priorityLabels.length).toBeGreaterThan(0);
  });

  it('renders due date dropdown', () => {
    const wrapper = mount(TaskFilterBar);
    const select = wrapper.find('select');
    expect(select.exists()).toBe(true);
    expect(select.text()).toContain('All');
    expect(select.text()).toContain('Overdue');
    expect(select.text()).toContain('Today');
  });

  it('shows "No filters applied" when no filters are active', () => {
    const wrapper = mount(TaskFilterBar);
    expect(wrapper.text()).toContain('No filters applied');
  });

  it('shows active filter count when filters are applied', async () => {
    const wrapper = mount(TaskFilterBar);
    const checkboxes = wrapper.findAll('input[type="checkbox"]');
    await checkboxes[0].setValue(true);
    expect(wrapper.text()).toContain('1 active');
  });

  it('renders clear filters button when filters are active', async () => {
    const wrapper = mount(TaskFilterBar);
    const checkboxes = wrapper.findAll('input[type="checkbox"]');
    await checkboxes[0].setValue(true);
    const clearBtn = wrapper.findAll('button').find((btn) => btn.text().includes('Clear Filters'));
    expect(clearBtn?.exists()).toBe(true);
  });
});

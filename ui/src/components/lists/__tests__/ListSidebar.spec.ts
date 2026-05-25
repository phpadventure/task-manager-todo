import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ListSidebar from '../ListSidebar.vue';

vi.mock('@/composables/useLists', () => {
  const { ref } = require('vue');
  return {
    useLists: () => ({
      fetchLists: vi.fn().mockResolvedValue(undefined),
      isLoading: ref(false),
    }),
  };
});

vi.mock('@/composables/useLocalStorage', () => ({
  useLocalStorage: () => ({
    load: vi.fn().mockReturnValue(null),
  }),
}));

describe('ListSidebar', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders sidebar with header', () => {
    const wrapper = mount(ListSidebar);
    expect(wrapper.find('h1').text()).toBe('My Lists');
  });

  it('shows empty state when no lists', async () => {
    const wrapper = mount(ListSidebar);
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('No lists yet');
  });

  it('renders + New List button', () => {
    const wrapper = mount(ListSidebar);
    const button = wrapper.findAll('button').find((btn) => btn.text().includes('+ New List'));
    expect(button?.exists()).toBe(true);
  });

  it('opens modal when + New List clicked', async () => {
    const wrapper = mount(ListSidebar);
    const button = wrapper.findAll('button').find((btn) => btn.text().includes('+ New List'));
    await button?.trigger('click');
    expect(wrapper.find('[class*="z-40"]').exists()).toBe(true);
  });

  it('renders CreateListModal component', () => {
    const wrapper = mount(ListSidebar);
    expect(wrapper.findComponent({ name: 'CreateListModal' }).exists()).toBe(true);
  });
});

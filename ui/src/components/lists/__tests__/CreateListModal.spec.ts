import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import CreateListModal from '../CreateListModal.vue';

vi.mock('@/composables/useLists', () => {
  const { ref } = require('vue');
  return {
    useLists: () => ({
      createList: vi.fn(),
      isCreating: ref(false),
    }),
  };
});

describe('CreateListModal', () => {
  let wrapper: any;

  beforeEach(() => {
    wrapper = mount(CreateListModal, {
      props: { isOpen: true },
    });
  });

  it('renders the modal when isOpen is true', () => {
    expect(wrapper.find('h2').text()).toBe('Create New List');
  });

  it('does not render modal when isOpen is false', () => {
    wrapper = mount(CreateListModal, {
      props: { isOpen: false },
    });
    expect(wrapper.find('h2').exists()).toBe(false);
  });

  it('renders title input field', () => {
    const input = wrapper.find('input[type="text"]');
    expect(input.exists()).toBe(true);
  });

  it('renders cancel and create buttons', () => {
    const buttons = wrapper.findAll('button');
    expect(buttons.length).toBeGreaterThanOrEqual(2);
    const cancelBtn = buttons.find((btn) => btn.text().includes('Cancel'));
    const createBtn = buttons.find((btn) => btn.text().includes('Create List'));
    expect(cancelBtn?.exists()).toBe(true);
    expect(createBtn?.exists()).toBe(true);
  });

  it('emits close event when cancel button clicked', async () => {
    const cancelBtn = wrapper.findAll('button').find((btn) => btn.text().includes('Cancel'));
    await cancelBtn?.trigger('click');
    expect(wrapper.emitted('close')).toBeTruthy();
  });

  it('submit button is disabled when title is empty', async () => {
    const submitBtn = wrapper.findAll('button').find((btn) => btn.text().includes('Create List'));
    expect(submitBtn?.attributes('disabled')).toBeDefined();
  });

  it('submit button is enabled when title is filled', async () => {
    const input = wrapper.find('input[type="text"]');
    await input.setValue('Test List');
    const submitBtn = wrapper.findAll('button').find((btn) => btn.text().includes('Create List'));
    expect(submitBtn?.attributes('disabled')).toBeUndefined();
  });
});

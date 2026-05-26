import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Modal from '../Modal.vue';

describe('Modal', () => {
  it('renders nothing when modelValue is false', () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: false,
      },
      global: {
        stubs: {
          Teleport: true,
          Transition: false,
        },
      },
    });
    expect(wrapper.find('.fixed').exists()).toBe(false);
  });

  it('renders modal when modelValue is true', () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
      global: {
        stubs: {
          Teleport: true,
          Transition: false,
        },
      },
    });
    expect(wrapper.find('.fixed').exists()).toBe(true);
  });

  it('renders modal body slot', () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
      slots: {
        default: 'Modal content',
      },
      global: {
        stubs: {
          Teleport: true,
          Transition: false,
        },
      },
    });
    expect(wrapper.text()).toContain('Modal content');
  });

  it('renders header slot', () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
      slots: {
        header: 'Modal Title',
      },
      global: {
        stubs: {
          Teleport: true,
          Transition: false,
        },
      },
    });
    expect(wrapper.text()).toContain('Modal Title');
  });

  it('renders footer slot', () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
      slots: {
        footer: 'Footer content',
      },
      global: {
        stubs: {
          Teleport: true,
          Transition: false,
        },
      },
    });
    expect(wrapper.text()).toContain('Footer content');
  });

  it('renders close button', () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
      slots: {
        header: 'Title',
      },
      global: {
        stubs: {
          Teleport: true,
          Transition: false,
        },
      },
    });
    const closeButton = wrapper.find('button');
    expect(closeButton.exists()).toBe(true);
  });

  it('emits update:modelValue on backdrop click', async () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
    });
    const backdrop = wrapper.find('.absolute.inset-0');
    await backdrop.trigger('click');
    expect(wrapper.emitted('update:modelValue')).toBeTruthy();
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false]);
  });

  it('emits update:modelValue on close button click', async () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
      slots: {
        header: 'Title',
      },
    });
    const closeButton = wrapper.find('button');
    await closeButton.trigger('click');
    expect(wrapper.emitted('update:modelValue')).toBeTruthy();
  });

  it('prevents backdrop click when closeOnBackdropClick is false', async () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
        closeOnBackdropClick: false,
      },
    });
    const backdrop = wrapper.find('.absolute.inset-0');
    await backdrop.trigger('click');
    expect(wrapper.emitted('update:modelValue')).toBeFalsy();
  });

  it('closes on escape key press', async () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
    });
    const modal = wrapper.find('[role="dialog"]');
    await modal.trigger('keydown.esc');
    expect(wrapper.emitted('update:modelValue')).toBeTruthy();
  });

  it('has modal role', () => {
    const wrapper = mount(Modal, {
      props: {
        modelValue: true,
      },
    });
    const modal = wrapper.find('[role="dialog"]');
    expect(modal.exists()).toBe(true);
    expect(modal.attributes('aria-modal')).toBe('true');
  });
});

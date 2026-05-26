import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Input from '../Input.vue';

describe('Input', () => {
  it('renders input with default type', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
      },
    });
    const input = wrapper.find('input');
    expect(input.exists()).toBe(true);
    expect(input.attributes('type')).toBe('text');
  });

  it('renders label when provided', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
        label: 'Task Title',
      },
    });
    expect(wrapper.text()).toContain('Task Title');
  });

  it('shows required asterisk', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
        label: 'Title',
        required: true,
      },
    });
    expect(wrapper.find('span').classes()).toContain('text-red-500');
  });

  it('displays placeholder text', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
        placeholder: 'Enter task title',
      },
    });
    const input = wrapper.find('input');
    expect(input.attributes('placeholder')).toBe('Enter task title');
  });

  it('emits update:modelValue on input', async () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
      },
    });
    const input = wrapper.find('input');
    await input.setValue('New text');
    expect(wrapper.emitted('update:modelValue')).toBeTruthy();
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['New text']);
  });

  it('emits blur event', async () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
      },
    });
    const input = wrapper.find('input');
    await input.trigger('blur');
    expect(wrapper.emitted('blur')).toBeTruthy();
  });

  it('shows error message when provided', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
        error: 'This field is required',
      },
    });
    expect(wrapper.text()).toContain('This field is required');
    expect(wrapper.find('p').classes()).toContain('text-red-600');
  });

  it('applies error styling to input', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
        error: 'Error message',
      },
    });
    const input = wrapper.find('input');
    expect(input.classes()).toContain('border-red-500');
  });

  it('disables input when disabled prop is true', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
        disabled: true,
      },
    });
    const input = wrapper.find('input') as any;
    expect(input.element.disabled).toBe(true);
  });

  it('handles different input types', () => {
    const types = ['email', 'date', 'number'];
    types.forEach((type) => {
      const wrapper = mount(Input, {
        props: {
          modelValue: '',
          type,
        },
      });
      const input = wrapper.find('input');
      expect(input.attributes('type')).toBe(type);
    });
  });
});

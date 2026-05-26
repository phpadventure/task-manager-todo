import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Button from '../Button.vue';

describe('Button', () => {
  it('renders button with default props', () => {
    const wrapper = mount(Button);
    const button = wrapper.find('button');
    expect(button.exists()).toBe(true);
    expect(button.classes()).toContain('bg-blue-600'); // primary variant
  });

  it('renders button text via slot', () => {
    const wrapper = mount(Button, {
      slots: {
        default: 'Click me',
      },
    });
    expect(wrapper.text()).toBe('Click me');
  });

  it('applies variant classes correctly', () => {
    const variants = ['primary', 'secondary', 'danger', 'ghost'];

    variants.forEach((variant) => {
      const wrapper = mount(Button, {
        props: { variant: variant as any },
      });
      const button = wrapper.find('button');
      expect(button.exists()).toBe(true);
    });
  });

  it('applies size classes correctly', () => {
    const wrapper = mount(Button, {
      props: { size: 'lg' },
    });
    const button = wrapper.find('button');
    expect(button.classes()).toContain('px-6');
    expect(button.classes()).toContain('py-3');
  });

  it('disables button when disabled prop is true', () => {
    const wrapper = mount(Button, {
      props: { disabled: true },
    });
    const button = wrapper.find('button') as any;
    expect(button.element.disabled).toBe(true);
  });

  it('shows loading spinner when loading is true', () => {
    const wrapper = mount(Button, {
      props: { loading: true },
    });
    expect(wrapper.find('span').exists()).toBe(true);
    expect(wrapper.classes()).toContain('animate-spin');
  });

  it('emits click event', async () => {
    const wrapper = mount(Button);
    await wrapper.find('button').trigger('click');
    expect(wrapper.emitted('click')).toBeTruthy();
  });

  it('disables button when loading is true', () => {
    const wrapper = mount(Button, {
      props: { loading: true },
    });
    const button = wrapper.find('button') as any;
    expect(button.element.disabled).toBe(true);
  });

  it('accepts button type prop', () => {
    const wrapper = mount(Button, {
      props: { type: 'submit' },
    });
    const button = wrapper.find('button');
    expect((button.element as HTMLButtonElement).type).toBe('submit');
  });
});

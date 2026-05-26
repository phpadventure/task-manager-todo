import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import Badge from '../Badge.vue';

describe('Badge', () => {
  it('renders badge with default type', () => {
    const wrapper = mount(Badge, {
      slots: {
        default: 'Primary',
      },
    });
    expect(wrapper.text()).toBe('Primary');
    expect(wrapper.classes()).toContain('bg-blue-100');
  });

  it('applies correct classes for success type', () => {
    const wrapper = mount(Badge, {
      props: { type: 'success' },
      slots: {
        default: 'Success',
      },
    });
    expect(wrapper.classes()).toContain('bg-green-100');
    expect(wrapper.classes()).toContain('text-green-800');
  });

  it('applies correct classes for warning type', () => {
    const wrapper = mount(Badge, {
      props: { type: 'warning' },
      slots: {
        default: 'Warning',
      },
    });
    expect(wrapper.classes()).toContain('bg-amber-100');
    expect(wrapper.classes()).toContain('text-amber-800');
  });

  it('applies correct classes for danger type', () => {
    const wrapper = mount(Badge, {
      props: { type: 'danger' },
      slots: {
        default: 'Danger',
      },
    });
    expect(wrapper.classes()).toContain('bg-red-100');
    expect(wrapper.classes()).toContain('text-red-800');
  });

  it('renders badge with slot content', () => {
    const wrapper = mount(Badge, {
      slots: {
        default: 'Custom Badge',
      },
    });
    expect(wrapper.text()).toBe('Custom Badge');
  });

  it('applies base badge classes', () => {
    const wrapper = mount(Badge);
    expect(wrapper.classes()).toContain('inline-flex');
    expect(wrapper.classes()).toContain('px-2');
    expect(wrapper.classes()).toContain('rounded-full');
  });
});

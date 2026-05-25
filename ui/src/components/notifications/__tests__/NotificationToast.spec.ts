import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import NotificationToast from '../NotificationToast.vue';

vi.mock('@/utils/dateUtils', () => ({
  getDueDateLabel: (date: string) => {
    const today = new Date().toISOString().split('T')[0];
    if (date === today) return 'Today';
    if (date < today) return 'Overdue';
    return date;
  },
}));

describe('NotificationToast', () => {
  const mockNotification = {
    id: 'notif-1',
    taskId: 'task-1',
    title: 'Buy groceries',
    reason: 'overdue' as const,
    dueDate: '2026-05-20',
    priority: 'HIGH' as const,
    sentAt: '2026-05-25T10:00:00Z',
  };

  it('renders notification with title', () => {
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    expect(wrapper.text()).toContain('Buy groceries');
  });

  it('renders reason badge', () => {
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    expect(wrapper.text()).toContain('Overdue');
  });

  it('renders priority badge', () => {
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    expect(wrapper.text()).toContain('High');
  });

  it('renders due date', () => {
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    expect(wrapper.text()).toContain('Due:');
  });

  it('renders acknowledge and dismiss buttons', () => {
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    const buttons = wrapper.findAll('button');
    expect(buttons.length).toBeGreaterThanOrEqual(3); // Got it, Dismiss, and close X
  });

  it('emits acknowledge event when Got it button clicked', async () => {
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    const buttons = wrapper.findAll('button');
    const gotItBtn = buttons.find((btn) => btn.text().includes('Got it'));
    await gotItBtn?.trigger('click');
    expect(wrapper.emitted('acknowledge')).toBeTruthy();
  });

  it('hides toast when dismissed', async () => {
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    const dismissBtn = wrapper.findAll('button').find((btn) => btn.text().includes('Dismiss'));
    await dismissBtn?.trigger('click');
    expect(wrapper.html()).not.toContain('Buy groceries');
  });

  it('hides toast when close button clicked', async () => {
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    const closeBtn = wrapper.findAll('button').find((btn) => btn.text() === '✕');
    await closeBtn?.trigger('click');
    expect(wrapper.html()).not.toContain('Buy groceries');
  });

  it('auto-dismisses after 5 seconds', async () => {
    vi.useFakeTimers();
    const wrapper = mount(NotificationToast, {
      props: { notification: mockNotification },
    });
    expect(wrapper.text()).toContain('Buy groceries');
    vi.advanceTimersByTime(5000);
    await flushPromises();
    expect(wrapper.html()).not.toContain('Buy groceries');
    vi.useRealTimers();
  });

  it('maps overdue reason to correct label', () => {
    const wrapper = mount(NotificationToast, {
      props: {
        notification: {
          ...mockNotification,
          reason: 'overdue',
        },
      },
    });
    expect(wrapper.text()).toContain('Overdue');
  });

  it('maps due-soon reason to correct label', () => {
    const wrapper = mount(NotificationToast, {
      props: {
        notification: {
          ...mockNotification,
          reason: 'due-soon',
        },
      },
    });
    expect(wrapper.text()).toContain('Due Soon');
  });
});

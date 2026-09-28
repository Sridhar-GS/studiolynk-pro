import axios from 'axios';
import { ApiResponse, AppNotification, UnreadNotificationCount } from '../types';

const API_BASE = '/api/notifications';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const notificationService = {
  async getNotifications(unreadOnly = false): Promise<AppNotification[]> {
    const res = await axios.get<ApiResponse<AppNotification[]>>(API_BASE, {
      params: { unreadOnly },
      headers: getAuthHeaders(),
    });
    return res.data.data || [];
  },

  async getUnreadCount(): Promise<number> {
    const res = await axios.get<ApiResponse<UnreadNotificationCount>>(`${API_BASE}/unread-count`, {
      headers: getAuthHeaders(),
    });
    return res.data.data?.count || 0;
  },

  async markAsRead(id: number): Promise<AppNotification> {
    const res = await axios.patch<ApiResponse<AppNotification>>(`${API_BASE}/${id}/read`, {}, {
      headers: getAuthHeaders(),
    });
    return res.data.data;
  },

  async markAllAsRead(): Promise<void> {
    await axios.patch(`${API_BASE}/read-all`, {}, {
      headers: getAuthHeaders(),
    });
  },
};

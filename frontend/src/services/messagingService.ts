import axios from 'axios';
import { Client } from '@stomp/stompjs';
import { ApiResponse, Conversation, ChatMessage } from '../types';

const API_BASE = '/api/conversations';

const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

export const messagingService = {
  // REST API Methods
  async getConversations(): Promise<Conversation[]> {
    const res = await axios.get<ApiResponse<Conversation[]>>(API_BASE, {
      headers: getAuthHeaders(),
    });
    return res.data.data;
  },

  async getConversationById(id: number): Promise<Conversation> {
    const res = await axios.get<ApiResponse<Conversation>>(`${API_BASE}/${id}`, {
      headers: getAuthHeaders(),
    });
    return res.data.data;
  },

  async getOrCreateConversation(requirementId: number, freelancerId?: number): Promise<Conversation> {
    const params = freelancerId ? { freelancerId } : {};
    const res = await axios.get<ApiResponse<Conversation>>(`${API_BASE}/requirement/${requirementId}`, {
      params,
      headers: getAuthHeaders(),
    });
    return res.data.data;
  },

  async getMessages(conversationId: number): Promise<ChatMessage[]> {
    const res = await axios.get<ApiResponse<ChatMessage[]>>(`${API_BASE}/${conversationId}/messages`, {
      headers: getAuthHeaders(),
    });
    return res.data.data;
  },

  async sendMessage(conversationId: number, content: string): Promise<ChatMessage> {
    const res = await axios.post<ApiResponse<ChatMessage>>(
      `${API_BASE}/${conversationId}/messages`,
      { content },
      { headers: getAuthHeaders() }
    );
    return res.data.data;
  },

  async markAsRead(conversationId: number): Promise<void> {
    await axios.patch(`${API_BASE}/${conversationId}/read`, {}, {
      headers: getAuthHeaders(),
    });
  },

  // STOMP WebSocket Client (Phase 11: MSG-002)
  createStompClient(
    onConnect?: () => void,
    onDisconnect?: () => void,
    onError?: (err: any) => void
  ): Client {
    const token = localStorage.getItem('token');
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const brokerURL = `${protocol}//${window.location.host}/ws`;

    const client = new Client({
      brokerURL: brokerURL,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: () => {},
      onConnect: () => {
        console.info('[STOMP] Connected to WebSocket broker');
        if (onConnect) onConnect();
      },
      onDisconnect: () => {
        console.info('[STOMP] Disconnected from WebSocket broker');
        if (onDisconnect) onDisconnect();
      },
      onStompError: (frame) => {
        console.error('[STOMP Error]', frame.headers['message'], frame.body);
        if (onError) onError(frame);
      },
      onWebSocketError: (event) => {
        console.error('[STOMP WS Error]', event);
        if (onError) onError(event);
      },
    });

    return client;
  },
};

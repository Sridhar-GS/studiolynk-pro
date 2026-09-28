import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import {
  MessageSquare,
  Send,
  Search,
  Calendar,
  Check,
  CheckCheck,
  ArrowLeft,
  Wifi,
  WifiOff,
  User,
  Building2,
  FileText,
  AlertCircle
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { messagingService } from '../../services/messagingService';
import { Conversation, ChatMessage } from '../../types';

export const MessagesPage: React.FC = () => {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  // State
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConversation, setActiveConversation] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputContent, setInputContent] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [loadingConversations, setLoadingConversations] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // References
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const stompClientRef = useRef<any>(null);
  const subscriptionRef = useRef<any>(null);

  const initialConversationId = searchParams.get('conversationId');
  const initialRequirementId = searchParams.get('requirementId');
  const initialFreelancerId = searchParams.get('freelancerId');

  // Scroll to bottom
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // 1. Fetch conversations on load
  const loadConversations = async (selectConvId?: number) => {
    try {
      const data = await messagingService.getConversations();
      setConversations(data || []);

      if (selectConvId) {
        const found = data.find((c) => c.id === selectConvId);
        if (found) {
          setActiveConversation(found);
        }
      } else if (!activeConversation && data.length > 0 && !initialRequirementId) {
        // Default to first conversation on desktop
        if (window.innerWidth >= 768) {
          setActiveConversation(data[0]);
        }
      }
    } catch (err: any) {
      console.error('Failed to load conversations:', err);
      setError('Unable to load conversations.');
    } finally {
      setLoadingConversations(false);
    }
  };

  useEffect(() => {
    const init = async () => {
      setLoadingConversations(true);
      if (initialRequirementId) {
        try {
          const conv = await messagingService.getOrCreateConversation(
            Number(initialRequirementId),
            initialFreelancerId ? Number(initialFreelancerId) : undefined
          );
          setActiveConversation(conv);
          await loadConversations(conv.id);
          return;
        } catch (err) {
          console.error('Failed to initiate conversation from params:', err);
        }
      }

      await loadConversations(initialConversationId ? Number(initialConversationId) : undefined);
    };

    init();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialRequirementId, initialFreelancerId, initialConversationId]);

  // 2. Initialize STOMP WebSocket Client (MSG-002)
  useEffect(() => {
    const client = messagingService.createStompClient(
      () => setIsConnected(true),
      () => setIsConnected(false),
      () => setIsConnected(false)
    );

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
      }
      client.deactivate();
    };
  }, []);

  // 3. Load messages & subscribe when active conversation changes
  useEffect(() => {
    if (!activeConversation) {
      setMessages([]);
      return;
    }

    const fetchMessages = async () => {
      setLoadingMessages(true);
      setError(null);
      try {
        const msgs = await messagingService.getMessages(activeConversation.id);
        setMessages(msgs || []);
        // Update local conversation unread count
        setConversations((prev) =>
          prev.map((c) => (c.id === activeConversation.id ? { ...c, unreadCount: 0 } : c))
        );
      } catch (err) {
        console.error('Failed to load messages for conversation:', err);
        setError('Failed to load messages.');
      } finally {
        setLoadingMessages(false);
        setTimeout(scrollToBottom, 100);
      }
    };

    fetchMessages();

    // Subscribe to STOMP topic for this conversation
    if (stompClientRef.current) {
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
      }

      const subscribeToTopic = () => {
        if (stompClientRef.current && stompClientRef.current.connected) {
          subscriptionRef.current = stompClientRef.current.subscribe(
            `/topic/conversation.${activeConversation.id}`,
            (messageFrame: any) => {
              try {
                const newMsg: ChatMessage = JSON.parse(messageFrame.body);
                setMessages((prev) => {
                  if (prev.some((m) => m.id === newMsg.id)) return prev;
                  return [...prev, newMsg];
                });
                setTimeout(scrollToBottom, 50);

                // If not sent by current user, mark as read
                if (user && newMsg.senderEmail !== user.email) {
                  messagingService.markAsRead(activeConversation.id).catch(console.error);
                }
              } catch (e) {
                console.error('Error parsing inbound message:', e);
              }
            }
          );
        }
      };

      if (stompClientRef.current.connected) {
        subscribeToTopic();
      } else {
        const checkTimer = setInterval(() => {
          if (stompClientRef.current?.connected) {
            subscribeToTopic();
            clearInterval(checkTimer);
          }
        }, 300);
        setTimeout(() => clearInterval(checkTimer), 5000);
      }
    }
  }, [activeConversation, user]);

  // 4. Send Message (MSG-003, MSG-005)
  const handleSendMessage = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputContent.trim() || !activeConversation || sending) return;

    const contentToSend = inputContent.trim();
    setInputContent('');
    setSending(true);

    try {
      // Send via REST endpoint (which also broadcasts over WebSocket STOMP)
      const sent = await messagingService.sendMessage(activeConversation.id, contentToSend);
      setMessages((prev) => {
        if (prev.some((m) => m.id === sent.id)) return prev;
        return [...prev, sent];
      });

      // Update conversations list last message
      setConversations((prev) =>
        prev.map((c) =>
          c.id === activeConversation.id
            ? {
                ...c,
                lastMessage: sent.content,
                lastMessageAt: sent.sentAt,
              }
            : c
        )
      );

      setTimeout(scrollToBottom, 50);
    } catch (err: any) {
      console.error('Failed to send message:', err);
      setError(err.response?.data?.message || 'Failed to send message.');
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  // Filter conversations
  const filteredConversations = conversations.filter((c) => {
    const query = searchQuery.toLowerCase();
    const otherParty = user?.role === 'STUDIO' ? c.freelancerName : c.studioName;
    return (
      otherParty?.toLowerCase().includes(query) ||
      c.eventName?.toLowerCase().includes(query) ||
      c.lastMessage?.toLowerCase().includes(query)
    );
  });

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col selection:bg-teal-500 selection:text-slate-950">
      {/* Top Header Banner */}
      <div className="border-b border-slate-800 bg-slate-900/60 px-4 sm:px-8 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400">
            <MessageSquare className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-base font-bold text-white tracking-tight">Requirement Chat &amp; Negotiation</h1>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-teal-500/20 text-teal-300 border border-teal-500/30">
                Phase 11 (MSG-001 - MSG-006)
              </span>
            </div>
            <p className="text-[11px] text-slate-400">Direct real-time communication for shoot coordination</p>
          </div>
        </div>

        {/* Live WebSocket Status Pill */}
        <div className="flex items-center gap-2 text-xs">
          <div
            className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-medium border ${
              isConnected
                ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                : 'bg-slate-800 text-slate-400 border-slate-700'
            }`}
          >
            {isConnected ? (
              <>
                <Wifi className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
                <span className="hidden sm:inline">WebSocket Live</span>
              </>
            ) : (
              <>
                <WifiOff className="w-3.5 h-3.5 text-slate-400" />
                <span className="hidden sm:inline">Connecting WS...</span>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Main Container */}
      <div className="flex-1 flex max-w-7xl w-full mx-auto overflow-hidden" style={{ height: 'calc(100vh - 120px)' }}>
        {/* Left Column: Conversation Directory */}
        <div
          className={`w-full md:w-80 lg:w-96 border-r border-slate-800 flex flex-col bg-slate-900/40 shrink-0 ${
            activeConversation ? 'hidden md:flex' : 'flex'
          }`}
        >
          {/* Search Box */}
          <div className="p-3 border-b border-slate-800">
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type="text"
                placeholder="Search conversations..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 bg-slate-800/80 border border-slate-700 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-teal-500 transition"
              />
            </div>
          </div>

          {/* Conversation List */}
          <div className="flex-1 overflow-y-auto divide-y divide-slate-800/60">
            {loadingConversations ? (
              <div className="py-12 flex flex-col items-center justify-center space-y-2">
                <div className="w-6 h-6 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
                <p className="text-xs text-slate-400">Loading conversations...</p>
              </div>
            ) : filteredConversations.length === 0 ? (
              <div className="py-12 text-center px-4 space-y-2">
                <MessageSquare className="w-8 h-8 text-slate-600 mx-auto" />
                <p className="text-xs font-semibold text-slate-300">No conversations found</p>
                <p className="text-[11px] text-slate-500">
                  Conversations are initiated when a studio sends a work request to a creator.
                </p>
              </div>
            ) : (
              filteredConversations.map((conv) => {
                const isSelected = activeConversation?.id === conv.id;
                const counterPartyName = user?.role === 'STUDIO' ? conv.freelancerName : conv.studioName;
                const photoUrl = user?.role === 'STUDIO' ? conv.freelancerPhotoUrl : conv.studioLogoUrl;

                return (
                  <button
                    key={conv.id}
                    onClick={() => {
                      setActiveConversation(conv);
                      setSearchParams({ conversationId: String(conv.id) });
                    }}
                    className={`w-full text-left p-3.5 flex items-start gap-3 transition-colors ${
                      isSelected
                        ? 'bg-teal-500/10 border-l-2 border-teal-400'
                        : 'hover:bg-slate-800/40'
                    }`}
                  >
                    <div className="w-10 h-10 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center overflow-hidden shrink-0">
                      {photoUrl ? (
                        <img src={photoUrl} alt={counterPartyName} className="w-full h-full object-cover" />
                      ) : user?.role === 'STUDIO' ? (
                        <User className="w-5 h-5 text-teal-400" />
                      ) : (
                        <Building2 className="w-5 h-5 text-indigo-400" />
                      )}
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-0.5">
                        <span className="text-xs font-bold text-white truncate">
                          {counterPartyName || `Creator #${conv.freelancerId}`}
                        </span>
                        {conv.lastMessageAt && (
                          <span className="text-[10px] text-slate-500 shrink-0">
                            {new Date(conv.lastMessageAt).toLocaleTimeString([], {
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                          </span>
                        )}
                      </div>

                      <div className="flex items-center gap-1.5 text-[11px] text-teal-300/80 mb-1">
                        <FileText className="w-3 h-3 shrink-0" />
                        <span className="truncate">{conv.eventName}</span>
                      </div>

                      <div className="flex items-center justify-between">
                        <p className="text-[11px] text-slate-400 truncate max-w-[190px]">
                          {conv.lastMessage || 'No messages yet. Say hello!'}
                        </p>
                        {conv.unreadCount > 0 && (
                          <span className="px-1.5 py-0.2 bg-teal-500 text-slate-950 font-bold rounded-full text-[10px] shrink-0">
                            {conv.unreadCount}
                          </span>
                        )}
                      </div>
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* Right Column: Active Chat Area */}
        <div className={`flex-1 flex flex-col bg-slate-950 ${!activeConversation ? 'hidden md:flex' : 'flex'}`}>
          {activeConversation ? (
            <>
              {/* Chat Top Bar */}
              <div className="p-3.5 border-b border-slate-800 bg-slate-900/40 flex items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => setActiveConversation(null)}
                    className="md:hidden p-1.5 rounded-lg bg-slate-800 text-slate-400 hover:text-white"
                  >
                    <ArrowLeft className="w-4 h-4" />
                  </button>

                  <div className="w-9 h-9 rounded-xl bg-slate-800 border border-slate-700 flex items-center justify-center shrink-0">
                    {user?.role === 'STUDIO' ? (
                      <User className="w-4 h-4 text-teal-400" />
                    ) : (
                      <Building2 className="w-4 h-4 text-indigo-400" />
                    )}
                  </div>

                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-xs sm:text-sm font-bold text-white">
                        {user?.role === 'STUDIO'
                          ? activeConversation.freelancerName
                          : activeConversation.studioName}
                      </h2>
                      <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                        {activeConversation.requirementStatus}
                      </span>
                    </div>
                    <div className="flex items-center gap-2 text-[11px] text-slate-400">
                      <span className="text-teal-300">{activeConversation.eventName}</span>
                      <span>&bull;</span>
                      <span className="flex items-center gap-1">
                        <Calendar className="w-3 h-3 text-slate-500" />
                        {activeConversation.eventDate}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <Link
                    to={
                      user?.role === 'STUDIO'
                        ? `/studio/requirements/${activeConversation.requirementId}`
                        : `/freelancer/requests`
                    }
                    className="text-[11px] font-semibold text-teal-400 hover:text-teal-300 px-3 py-1.5 rounded-xl bg-slate-800/80 border border-slate-700 transition"
                  >
                    View Shoot Brief
                  </Link>
                </div>
              </div>

              {/* Error Alert */}
              {error && (
                <div className="m-3 p-2.5 bg-rose-500/10 border border-rose-500/30 rounded-xl flex items-center gap-2 text-rose-300 text-xs">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span>{error}</span>
                </div>
              )}

              {/* Messages Body */}
              <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {loadingMessages ? (
                  <div className="py-16 flex flex-col items-center justify-center space-y-2">
                    <div className="w-6 h-6 border-2 border-teal-400 border-t-transparent rounded-full animate-spin" />
                    <p className="text-xs text-slate-400">Loading messages...</p>
                  </div>
                ) : messages.length === 0 ? (
                  <div className="py-20 text-center space-y-3 max-w-sm mx-auto">
                    <div className="w-12 h-12 rounded-2xl bg-teal-500/10 border border-teal-500/30 flex items-center justify-center text-teal-400 mx-auto">
                      <MessageSquare className="w-6 h-6" />
                    </div>
                    <h3 className="text-sm font-bold text-white">Start the Discussion (MSG-005)</h3>
                    <p className="text-xs text-slate-400">
                      Coordinate shoot logistics, inquire about gear, or negotiate rates directly with {user?.role === 'STUDIO' ? 'the creator' : 'the studio owner'}.
                    </p>
                  </div>
                ) : (
                  messages.map((msg) => {
                    const isMine = user && msg.senderEmail === user.email;

                    return (
                      <div
                        key={msg.id}
                        className={`flex flex-col ${isMine ? 'items-end' : 'items-start'}`}
                      >
                        <div className="flex items-center gap-1.5 mb-1 px-1">
                          <span className="text-[10px] text-slate-400 font-medium">
                            {isMine ? 'You' : msg.senderName}
                          </span>
                          <span className="text-[10px] text-slate-600">&bull;</span>
                          <span className="text-[10px] text-slate-500">
                            {new Date(msg.sentAt).toLocaleTimeString([], {
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                          </span>
                        </div>

                        <div
                          className={`max-w-md sm:max-w-lg p-3 rounded-2xl text-xs leading-relaxed break-words shadow-md ${
                            isMine
                              ? 'bg-teal-600/30 border border-teal-500/40 text-teal-50 rounded-tr-sm'
                              : 'bg-slate-800/90 border border-slate-700/80 text-slate-100 rounded-tl-sm'
                          }`}
                        >
                          <p className="whitespace-pre-wrap">{msg.content}</p>

                          <div className="flex items-center justify-end gap-1 mt-1 text-[10px] text-slate-400">
                            {isMine && (
                              <span>
                                {msg.read ? (
                                  <CheckCheck className="w-3.5 h-3.5 text-teal-400 inline" />
                                ) : (
                                  <Check className="w-3.5 h-3.5 text-slate-500 inline" />
                                )}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Quick Negotiation Prompt Chips (MSG-005) */}
              <div className="px-4 py-2 bg-slate-900/60 border-t border-slate-800/80 flex items-center gap-2 overflow-x-auto text-[11px]">
                <span className="text-slate-500 shrink-0 font-medium">Quick Prompts:</span>
                <button
                  type="button"
                  onClick={() => setInputContent('Hi! Are you open to discussing the shoot timings and call schedule?')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 shrink-0 transition"
                >
                  Timings &amp; Schedule
                </button>
                <button
                  type="button"
                  onClick={() => setInputContent('Could you share which camera body and lenses you will be bringing?')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 shrink-0 transition"
                >
                  Gear Checklist
                </button>
                <button
                  type="button"
                  onClick={() => setInputContent('Is the offered compensation rate negotiable for this assignment?')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 shrink-0 transition"
                >
                  Negotiate Rate
                </button>
                <button
                  type="button"
                  onClick={() => setInputContent('Confirmed! Looking forward to covering this event.')}
                  className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 shrink-0 transition"
                >
                  Confirm Coverage
                </button>
              </div>

              {/* Message Composer Bar */}
              <form onSubmit={handleSendMessage} className="p-3.5 border-t border-slate-800 bg-slate-900/80">
                <div className="flex items-end gap-2">
                  <div className="flex-1 bg-slate-800/80 border border-slate-700 rounded-2xl p-2.5 focus-within:border-teal-500 transition">
                    <textarea
                      rows={2}
                      value={inputContent}
                      onChange={(e) => setInputContent(e.target.value)}
                      onKeyDown={handleKeyDown}
                      placeholder="Type your message... (Enter to send, Shift+Enter for newline)"
                      maxLength={4000}
                      className="w-full bg-transparent text-xs text-white placeholder-slate-500 resize-none focus:outline-none"
                    />
                    <div className="flex items-center justify-between text-[10px] text-slate-500 pt-1">
                      <span>{inputContent.length} / 4000</span>
                      <span>Text only (MSG-003)</span>
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={sending || !inputContent.trim()}
                    className="p-3 rounded-2xl bg-teal-500 hover:bg-teal-400 text-slate-950 font-bold shadow-md shadow-teal-500/20 disabled:opacity-50 transition shrink-0"
                  >
                    <Send className="w-4 h-4" />
                  </button>
                </div>
              </form>
            </>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center space-y-4">
              <div className="w-16 h-16 rounded-3xl bg-slate-900 border border-slate-800 flex items-center justify-center text-slate-600">
                <MessageSquare className="w-8 h-8" />
              </div>
              <h3 className="text-base font-bold text-white">Select a Conversation</h3>
              <p className="text-xs text-slate-400 max-w-sm">
                Choose a requirement chat from the left directory to review message history and negotiate shoot parameters.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

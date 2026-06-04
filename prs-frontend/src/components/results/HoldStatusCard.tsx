import type { HoldResponse, HoldStatus } from '../../types/hold';
import { releaseHold } from '../../services/holdApi';
import { useState } from 'react';

interface HoldStatusCardProps {
  hold: HoldResponse;
  isPolling: boolean;
  onReleased?: () => void;
}

const STATUS_LABELS: Record<HoldStatus, string> = {
  PENDING_PRICE: 'Calculating price...',
  ACTIVE: 'Hold active',
  EXPIRED: 'Hold expired',
  RELEASED: 'Hold released',
  FAILED: 'Pricing failed',
  CONVERTED: 'Converted to reservation',
};

const STATUS_COLORS: Record<HoldStatus, string> = {
  PENDING_PRICE: '#f59e0b',
  ACTIVE: '#22c55e',
  EXPIRED: '#6b7280',
  RELEASED: '#6b7280',
  FAILED: '#ef4444',
  CONVERTED: '#3b82f6',
};

export function HoldStatusCard({ hold, isPolling, onReleased }: HoldStatusCardProps) {
  const [releasing, setReleasing] = useState(false);
  const [releaseError, setReleaseError] = useState<string | null>(null);

  const handleRelease = async () => {
    setReleasing(true);
    setReleaseError(null);
    try {
      await releaseHold(hold.holdId);
      onReleased?.();
    } catch (err) {
      setReleaseError(err instanceof Error ? err.message : 'Failed to release hold');
    } finally {
      setReleasing(false);
    }
  };

  const color = STATUS_COLORS[hold.status];
  const canRelease = hold.status === 'PENDING_PRICE' || hold.status === 'ACTIVE';

  return (
    <div style={{ border: `2px solid ${color}`, borderRadius: 8, padding: 16, marginTop: 16 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontWeight: 'bold', color }}>
          {STATUS_LABELS[hold.status]}
          {isPolling && ' '}
          {isPolling && <span style={{ fontSize: 12 }}>⏳</span>}
        </span>
        {hold.confirmedPrice && (
          <span style={{ fontWeight: 'bold' }}>
            {hold.confirmedPrice.amount} {hold.confirmedPrice.currency}
          </span>
        )}
        {!hold.confirmedPrice && hold.estimatedPrice && (
          <span style={{ color: '#9ca3af' }}>
            Est. {hold.estimatedPrice.amount} {hold.estimatedPrice.currency}
          </span>
        )}
      </div>
      <div style={{ fontSize: 12, color: '#6b7280', marginTop: 8 }}>
        Expires: {new Date(hold.expiresAt).toLocaleTimeString()}
      </div>
      {releaseError && (
        <div style={{ color: '#ef4444', fontSize: 12, marginTop: 4 }}>{releaseError}</div>
      )}
      {canRelease && (
        <button
          onClick={handleRelease}
          disabled={releasing}
          style={{ marginTop: 12, padding: '6px 16px', cursor: releasing ? 'not-allowed' : 'pointer' }}
        >
          {releasing ? 'Releasing...' : 'Release Hold'}
        </button>
      )}
    </div>
  );
}

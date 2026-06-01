import { useState } from 'react';
import { useParkingSearch } from '../hooks/useParkingSearch';
import type { FacilityResult, SearchRequest } from '../types/catalog';

function FacilityResultCard({ facility }: { facility: FacilityResult }) {
  return (
    <div
      style={{
        border: '1px solid #ddd',
        borderRadius: '8px',
        padding: '16px',
        marginBottom: '12px',
        background: '#fff',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h3 style={{ margin: '0 0 4px' }}>{facility.name}</h3>
          <p style={{ margin: '0 0 4px', color: '#555' }}>
            {facility.city} — {facility.address}
          </p>
        </div>
        {facility.lowAvailabilityWarning && (
          <span
            style={{
              background: '#fff3cd',
              border: '1px solid #ffc107',
              borderRadius: '4px',
              padding: '4px 8px',
              fontSize: '12px',
              fontWeight: 600,
              color: '#856404',
              whiteSpace: 'nowrap',
            }}
          >
            ⚠ Low availability
          </span>
        )}
      </div>
      <p style={{ margin: '8px 0 4px', fontSize: '14px' }}>
        Available spots: <strong>{facility.availableSpots}</strong>
      </p>
      <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginTop: '8px' }}>
        {facility.tags.map((tag) => (
          <span
            key={tag}
            style={{
              background: '#e9ecef',
              borderRadius: '4px',
              padding: '2px 8px',
              fontSize: '12px',
              color: '#495057',
            }}
          >
            {tag.replace(/_/g, ' ')}
          </span>
        ))}
      </div>
    </div>
  );
}

export default function SearchPage() {
  const [text, setText] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [submittedParams, setSubmittedParams] = useState<SearchRequest | null>(null);

  const { data, isLoading, isError, error } = useParkingSearch(submittedParams);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!text.trim() || !checkIn || !checkOut) return;
    setSubmittedParams({ q: text.trim(), checkIn, checkOut });
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '32px 16px' }}>
      <h1 style={{ marginBottom: '24px' }}>Find Parking</h1>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px' }}>
        <label>
          Location
          <input
            type="text"
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="City or facility name"
            style={{ display: 'block', width: '100%', padding: '8px', marginTop: '4px', boxSizing: 'border-box' }}
            required
          />
        </label>
        <div style={{ display: 'flex', gap: '12px' }}>
          <label style={{ flex: 1 }}>
            Check-in
            <input
              type="datetime-local"
              value={checkIn}
              onChange={(e) => setCheckIn(e.target.value)}
              style={{ display: 'block', width: '100%', padding: '8px', marginTop: '4px', boxSizing: 'border-box' }}
              required
            />
          </label>
          <label style={{ flex: 1 }}>
            Check-out
            <input
              type="datetime-local"
              value={checkOut}
              onChange={(e) => setCheckOut(e.target.value)}
              style={{ display: 'block', width: '100%', padding: '8px', marginTop: '4px', boxSizing: 'border-box' }}
              required
            />
          </label>
        </div>
        <button
          type="submit"
          style={{
            padding: '10px 24px',
            background: '#0d6efd',
            color: '#fff',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            alignSelf: 'flex-start',
          }}
        >
          Search
        </button>
      </form>

      {isLoading && <p>Searching...</p>}

      {isError && (
        <p style={{ color: '#dc3545' }}>
          Error: {error?.message ?? 'Failed to load results'}
        </p>
      )}

      {data && data.length === 0 && !isLoading && (
        <p style={{ color: '#6c757d' }}>No parking facilities found for your search.</p>
      )}

      {data && data.length > 0 && (
        <div>
          <p style={{ marginBottom: '12px', color: '#6c757d' }}>{data.length} result(s)</p>
          {data.map((facility) => (
            <FacilityResultCard key={facility.facilityId} facility={facility} />
          ))}
        </div>
      )}
    </div>
  );
}

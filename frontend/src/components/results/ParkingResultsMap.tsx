import L from 'leaflet';
import { useEffect } from 'react';
import { MapContainer, Marker, Popup, TileLayer, useMap } from 'react-leaflet';
import type { FacilityResult } from '../../types/catalog';

interface ParkingResultsMapProps {
  facilities: FacilityResult[];
  onSelectFacility: (facilityId: string) => void;
  selectedFacilityId: string | null;
}

const selectedParkingIcon = L.divIcon({
  className: 'parking-marker parking-marker--selected',
  html: '<span>P</span>',
  iconAnchor: [18, 36],
  iconSize: [36, 36],
  popupAnchor: [0, -32],
});

const mutedParkingIcon = L.divIcon({
  className: 'parking-marker parking-marker--muted',
  html: '<span>P</span>',
  iconAnchor: [18, 36],
  iconSize: [36, 36],
  popupAnchor: [0, -32],
});

const parkingIcon = L.divIcon({
  className: 'parking-marker',
  html: '<span>P</span>',
  iconAnchor: [18, 36],
  iconSize: [36, 36],
  popupAnchor: [0, -32],
});

type LocatedFacilityResult = FacilityResult & {
  latitude: number;
  longitude: number;
};

function hasCoordinates(facility: FacilityResult): facility is LocatedFacilityResult {
  return facility.latitude != null && facility.longitude != null;
}

function getMapCenter(facilities: FacilityResult[]): [number, number] {
  const locatedFacilities = facilities.filter(hasCoordinates);

  if (locatedFacilities.length === 0) {
    return [40.4168, -3.7038];
  }

  const latitude =
    locatedFacilities.reduce((total, facility) => total + facility.latitude, 0) / locatedFacilities.length;
  const longitude =
    locatedFacilities.reduce((total, facility) => total + facility.longitude, 0) / locatedFacilities.length;

  return [latitude, longitude];
}

function SelectedFacilityFocus({
  facilities,
  selectedFacilityId,
}: {
  facilities: FacilityResult[];
  selectedFacilityId: string | null;
}) {
  const map = useMap();

  useEffect(() => {
    const selectedFacility = facilities.find((facility) => facility.facilityId === selectedFacilityId);

    if (!selectedFacility || !hasCoordinates(selectedFacility)) return;

    map.flyTo([selectedFacility.latitude, selectedFacility.longitude], Math.max(map.getZoom(), 15), {
      duration: 0.65,
    });
  }, [facilities, map, selectedFacilityId]);

  return null;
}

export function ParkingResultsMap({
  facilities,
  onSelectFacility,
  selectedFacilityId,
}: ParkingResultsMapProps) {
  const locatedFacilities = facilities.filter(hasCoordinates);
  const center = getMapCenter(facilities);
  const hasSelection = Boolean(selectedFacilityId);

  return (
    <MapContainer
      center={center}
      scrollWheelZoom={false}
      style={{ height: '100%', minHeight: 560, width: '100%' }}
      zoom={13}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <SelectedFacilityFocus facilities={facilities} selectedFacilityId={selectedFacilityId} />
      {locatedFacilities.map((facility) => (
        <Marker
          eventHandlers={{
            click: () => onSelectFacility(facility.facilityId),
          }}
          icon={
            facility.facilityId === selectedFacilityId
              ? selectedParkingIcon
              : hasSelection
                ? mutedParkingIcon
                : parkingIcon
          }
          key={facility.facilityId}
          position={[facility.latitude, facility.longitude]}
        >
          <Popup>
            <strong>{facility.name}</strong>
            <br />
            {facility.address}
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}

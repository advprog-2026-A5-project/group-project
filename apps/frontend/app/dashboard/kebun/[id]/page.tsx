"use client";

import { useEffect, useState, useCallback } from "react";
import { useParams } from "next/navigation";

interface Kebun {
  id: number;
  nama: string;
  kodeKebun: string;
  luas: number;
  wktGeometry: string;
  mandorName: string | null;
  supirNames: string[];
}

export default function KebunDetailPage() {
  const params = useParams();
  const id = params.id;
  const [kebun, setKebun] = useState<Kebun | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [supirSearch, setSupirSearch] = useState("");

  const fetchKebun = useCallback(async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem("token");
      const res = await fetch(`/api/kebun/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Kebun tidak ditemukan");
      const data = await res.json();
      setKebun(data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Terjadi kesalahan");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchKebun();
  }, [fetchKebun]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin w-10 h-10 border-4 border-[#D2691E] border-t-transparent rounded-full" />
      </div>
    );
  }

  if (error || !kebun) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-2xl p-8 text-center">
        <p className="text-red-600 font-semibold text-lg">{error || "Kebun tidak ditemukan"}</p>
        <a
          href="/dashboard"
          className="mt-4 inline-block px-6 py-2 bg-[#8B4513] text-white rounded-xl hover:bg-[#703810] transition-colors font-medium"
        >
          Kembali ke Dashboard
        </a>
      </div>
    );
  }

  // Parse WKT geometry for display
  const parseWkt = (wkt: string): { lat: number; lng: number }[] => {
    try {
      const match = wkt.match(/POLYGON\s*\(\((.*?)\)\)/);
      if (!match) return [];
      return match[1].split(",").map((pair) => {
        const [lng, lat] = pair.trim().split(/\s+/).map(Number);
        return { lat, lng };
      });
    } catch {
      return [];
    }
  };

  const coordinates = parseWkt(kebun.wktGeometry);

  // Filter supir by search term
  const filteredSupir = kebun.supirNames
    ? kebun.supirNames.filter((name) =>
        name.toLowerCase().includes(supirSearch.toLowerCase())
      )
    : [];

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <a
          href="/dashboard"
          className="text-[#D2691E] hover:text-[#8B4513] font-medium text-sm flex items-center gap-1 transition-colors"
        >
          ← Kembali ke Daftar
        </a>
        <a
          href={`/dashboard/kebun/${kebun.id}/edit`}
          className="px-5 py-2 bg-[#D2691E] text-white font-bold rounded-xl hover:bg-[#8B4513] transition-colors text-sm"
        >
          Edit Kebun
        </a>
      </div>

      {/* Main Card */}
      <div className="bg-white rounded-2xl shadow-md overflow-hidden border border-[#EEDDCC]">
        <div className="bg-gradient-to-r from-[#8B4513] to-[#D2691E] p-8">
          <h2 className="text-2xl font-bold text-white">{kebun.nama}</h2>
          <div className="flex items-center gap-4 text-white/70 mt-2 text-sm font-medium">
            <span>ID: #{kebun.id}</span>
            <span>•</span>
            <span className="bg-white/20 px-2 py-1 rounded-md">Kode: {kebun.kodeKebun}</span>
            <span>•</span>
            <span className="bg-white/20 px-2 py-1 rounded-md">Luas: {kebun.luas} Ha</span>
          </div>
        </div>

        <div className="p-8 space-y-8">
          {/* Mandor Status */}
          <div className="bg-[#FDF8F3] rounded-xl p-5 border border-[#EEDDCC]">
            <p className="text-xs font-bold text-[#8B6F5A] uppercase tracking-wider mb-2">
              Mandor
            </p>
            {kebun.mandorName ? (
              <p className="text-lg font-bold text-[#3D1C00]">
                ✓ {kebun.mandorName}
              </p>
            ) : (
              <p className="text-lg text-gray-400 italic">Belum di-assign</p>
            )}
          </div>

          {/* Supir List with Filter */}
          <div className="bg-[#FDF8F3] rounded-xl p-5 border border-[#EEDDCC]">
            <div className="flex items-center justify-between mb-3">
              <p className="text-xs font-bold text-[#8B6F5A] uppercase tracking-wider">
                Daftar Supir Truk ({kebun.supirNames?.length || 0})
              </p>
              {kebun.supirNames && kebun.supirNames.length > 0 && (
                <input
                  type="text"
                  placeholder="Cari nama supir..."
                  value={supirSearch}
                  onChange={(e) => setSupirSearch(e.target.value)}
                  className="px-3 py-1.5 text-sm border border-gray-200 rounded-lg outline-none focus:ring-2 focus:ring-[#D2691E] w-48 text-gray-900"
                />
              )}
            </div>
            {filteredSupir.length > 0 ? (
              <div className="flex flex-wrap gap-2">
                {filteredSupir.map((supir) => (
                  <span
                    key={supir}
                    className="inline-flex py-1 px-3 bg-blue-50 text-blue-700 rounded-lg text-sm font-bold shadow-sm border border-blue-100"
                  >
                    🚚 {supir}
                  </span>
                ))}
              </div>
            ) : (
              <p className="text-lg text-gray-400 italic">
                {supirSearch ? "Tidak ada supir yang cocok" : "Belum di-assign"}
              </p>
            )}
          </div>

          {/* Coordinates */}
          <div>
            <h3 className="text-sm font-bold text-[#5D2E0B] uppercase tracking-wider mb-3">
              Koordinat Area
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {coordinates.slice(0, 4).map((coord, i) => (
                <div
                  key={i}
                  className="flex items-center gap-3 bg-[#F5EFE7] rounded-xl p-4 border border-[#EEDDCC]"
                >
                  <span className="w-8 h-8 bg-[#D2691E] text-white rounded-lg flex items-center justify-center font-bold text-sm flex-shrink-0">
                    {i + 1}
                  </span>
                  <div className="text-sm">
                    <span className="text-[#8B6F5A]">Lat:</span>{" "}
                    <span className="font-mono font-semibold text-[#3D1C00]">{coord.lat}</span>
                    <span className="text-[#8B6F5A] ml-3">Lng:</span>{" "}
                    <span className="font-mono font-semibold text-[#3D1C00]">{coord.lng}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Raw WKT */}
          <div>
            <h3 className="text-sm font-bold text-[#5D2E0B] uppercase tracking-wider mb-2">
              WKT Geometry
            </h3>
            <pre className="bg-[#1a1a2e] text-green-400 p-4 rounded-xl text-xs overflow-x-auto font-mono">
              {kebun.wktGeometry}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
}

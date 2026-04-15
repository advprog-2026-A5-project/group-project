"use client";

import { useEffect, useState, FormEvent, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";

interface Coordinate {
  latitude: string;
  longitude: string;
}

export default function EditKebunPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id;

  const [nama, setNama] = useState("");
  const [kodeKebun, setKodeKebun] = useState("");
  const [luas, setLuas] = useState("");
  const [coords, setCoords] = useState<Coordinate[]>([
    { latitude: "", longitude: "" },
    { latitude: "", longitude: "" },
    { latitude: "", longitude: "" },
    { latitude: "", longitude: "" },
  ]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const fetchKebun = useCallback(async () => {
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`/api/kebun/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Kebun tidak ditemukan");
      const data = await res.json();

      setNama(data.nama);
      setKodeKebun(data.kodeKebun);
      setLuas(data.luas.toString());

      // Parse WKT to coordinates
      const match = data.wktGeometry.match(/POLYGON\s*\(\((.*?)\)\)/);
      if (match) {
        const pairs = match[1].split(",").slice(0, 4);
        const parsed = pairs.map((pair: string) => {
          const [lng, lat] = pair.trim().split(/\s+/);
          return { latitude: lat, longitude: lng };
        });
        setCoords(parsed);
      }
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "Gagal memuat data");
      router.push("/dashboard");
    } finally {
      setLoading(false);
    }
  }, [id, router]);

  useEffect(() => {
    fetchKebun();
  }, [fetchKebun]);

  const updateCoord = (index: number, field: "latitude" | "longitude", value: string) => {
    const newCoords = [...coords];
    newCoords[index] = { ...newCoords[index], [field]: value };
    setCoords(newCoords);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      const koordinat = coords.map((c) => ({
        latitude: parseFloat(c.latitude),
        longitude: parseFloat(c.longitude),
      }));

      for (const k of koordinat) {
        if (isNaN(k.latitude) || isNaN(k.longitude)) {
          alert("Semua koordinat harus berupa angka yang valid");
          setSubmitting(false);
          return;
        }
      }

      const token = localStorage.getItem("token");
      const res = await fetch(`/api/kebun/${id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ nama, luas: parseFloat(luas), koordinat }),
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Gagal memperbarui kebun");
      }

      alert("Kebun berhasil diperbarui!");
      router.push("/dashboard");
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "Gagal memperbarui");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin w-10 h-10 border-4 border-[#D2691E] border-t-transparent rounded-full" />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-6">
        <a
          href="/dashboard"
          className="text-[#D2691E] hover:text-[#8B4513] font-medium text-sm flex items-center gap-1 transition-colors"
        >
          ← Kembali ke Daftar
        </a>
      </div>

      <div className="bg-white rounded-2xl shadow-md overflow-hidden border border-[#EEDDCC]">
        <div className="bg-gradient-to-r from-[#8B4513] to-[#D2691E] p-8">
          <h2 className="text-2xl font-bold text-white">Edit Kebun</h2>
          <p className="text-white/70 mt-1">Perbarui data kebun #{id}</p>
        </div>

        <form onSubmit={handleSubmit} className="p-8 space-y-6">
          <div className="space-y-1">
            <label className="text-sm font-bold text-[#5D2E0B] ml-1">
              Nama Kebun
            </label>
            <input
              type="text"
              value={nama}
              onChange={(e) => setNama(e.target.value)}
              required
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-gray-900"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* Kode Kebun */}
            <div className="space-y-1">
              <label className="text-sm font-bold text-[#5D2E0B] ml-1 flex justify-between">
                Kode Kebun
                <span className="text-xs text-gray-400 font-normal">Tidak dapat diubah</span>
              </label>
              <input
                type="text"
                value={kodeKebun}
                disabled
                className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-500 cursor-not-allowed outline-none transition-all"
              />
            </div>

            {/* Luas */}
            <div className="space-y-1">
              <label className="text-sm font-bold text-[#5D2E0B] ml-1">
                Luas (Hektar)
              </label>
              <input
                type="number"
                step="any"
                value={luas}
                onChange={(e) => setLuas(e.target.value)}
                required
                min="0.1"
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-gray-900"
              />
            </div>
          </div>

          <div className="space-y-4">
            <label className="text-sm font-bold text-[#5D2E0B] ml-1">
              Koordinat (4 Titik)
            </label>
            {coords.map((coord, i) => (
              <div
                key={i}
                className="flex gap-3 items-center bg-[#FDF8F3] p-4 rounded-xl border border-[#EEDDCC]"
              >
                <span className="w-8 h-8 bg-[#D2691E] text-white rounded-lg flex items-center justify-center font-bold text-sm flex-shrink-0">
                  {i + 1}
                </span>
                <div className="flex-1 grid grid-cols-2 gap-3">
                  <input
                    type="text"
                    value={coord.latitude}
                    onChange={(e) => updateCoord(i, "latitude", e.target.value)}
                    required
                    className="px-3 py-2 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-sm text-gray-900"
                    placeholder="Latitude"
                  />
                  <input
                    type="text"
                    value={coord.longitude}
                    onChange={(e) => updateCoord(i, "longitude", e.target.value)}
                    required
                    className="px-3 py-2 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-sm text-gray-900"
                    placeholder="Longitude"
                  />
                </div>
              </div>
            ))}
          </div>

          <div className="flex gap-3">
            <a
              href="/dashboard"
              className="flex-1 py-3 border-2 border-gray-200 text-gray-600 font-bold rounded-xl hover:bg-gray-50 transition-colors text-center"
            >
              Batal
            </a>
            <button
              type="submit"
              disabled={submitting}
              className={`flex-1 bg-gradient-to-r from-[#8B4513] to-[#D2691E] text-white font-bold py-3 rounded-xl shadow-lg transition-all ${submitting
                  ? "opacity-50 cursor-not-allowed"
                  : "hover:shadow-xl active:scale-[0.99]"
                }`}
            >
              {submitting ? "Menyimpan..." : "Simpan Perubahan"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

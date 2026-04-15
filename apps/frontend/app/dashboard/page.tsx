"use client";

import { useEffect, useState, useCallback } from "react";

interface Kebun {
  id: number;
  nama: string;
  kodeKebun: string;
  luas: number;
  wktGeometry: string;
  mandorName: string | null;
  supirNames: string[];
}

export default function DashboardPage() {
  const [kebunList, setKebunList] = useState<Kebun[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Search state
  const [searchNama, setSearchNama] = useState("");
  const [searchKode, setSearchKode] = useState("");

  // Modal state for assign/unassign
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState<
    "assign-mandor" | "assign-supir" | "unassign-mandor" | "unassign-supir" | null
  >(null);
  const [selectedKebun, setSelectedKebun] = useState<Kebun | null>(null);
  const [targetSupir, setTargetSupir] = useState<string | null>(null);
  const [inputName, setInputName] = useState("");
  const [targetKebunId, setTargetKebunId] = useState<string>("");
  const [modalLoading, setModalLoading] = useState(false);

  const fetchKebun = useCallback(async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem("token");

      const queryParams = new URLSearchParams();
      if (searchNama) queryParams.append("nama", searchNama);
      if (searchKode) queryParams.append("kodeKebun", searchKode);

      const res = await fetch(`/api/kebun?${queryParams.toString()}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Gagal memuat data kebun");
      const data = await res.json();
      setKebunList(data);
      setError(null);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Terjadi kesalahan");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchKebun();
  }, [fetchKebun]);

  useEffect(() => {
    const timeout = setTimeout(() => fetchKebun(), 300);
    return () => clearTimeout(timeout);
  }, [searchNama, searchKode, fetchKebun]);

  const handleDelete = async (id: number) => {
    if (!confirm("Yakin ingin menghapus kebun ini?")) return;
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(`/api/kebun/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Gagal menghapus kebun");
      }
      fetchKebun();
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "Gagal menghapus");
    }
  };

  const openModal = (
    kebun: Kebun,
    type: "assign-mandor" | "assign-supir" | "unassign-mandor" | "unassign-supir",
    supirName?: string
  ) => {
    setSelectedKebun(kebun);
    setModalType(type);
    setTargetSupir(supirName || null);
    setInputName("");
    setTargetKebunId("");
    setModalOpen(true);
  };

  const handleModalSubmit = async () => {
    if (!selectedKebun || !modalType) return;
    setModalLoading(true);

    try {
      const token = localStorage.getItem("token");
      const url = `/api/kebun/${selectedKebun.id}/${modalType}`;
      let body: Record<string, unknown> = {};

      if (modalType.startsWith("assign")) {
        body = { name: inputName };
      } else {
        // Unassign = move to another kebun
        body = {
          targetKebunId: parseInt(targetKebunId),
          ...(modalType === "unassign-supir" && targetSupir ? { supirName: targetSupir } : {}),
        };
      }

      const res = await fetch(url, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(body),
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Gagal memproses permintaan");
      }

      setModalOpen(false);
      fetchKebun();
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "Gagal memproses");
    } finally {
      setModalLoading(false);
    }
  };

  const getModalTitle = () => {
    switch (modalType) {
      case "assign-mandor":
        return "Assign Mandor";
      case "assign-supir":
        return "Assign Supir";
      case "unassign-mandor":
        return "Pindahkan Mandor ke Kebun Lain";
      case "unassign-supir":
        return "Pindahkan Supir ke Kebun Lain";
      default:
        return "";
    }
  };

  const isUnassign = modalType?.startsWith("unassign");
  const canSubmit = isUnassign ? !!targetKebunId : !!inputName.trim();

  // Get available target kebuns (exclude current kebun)
  const availableTargets = kebunList.filter((k) => k.id !== selectedKebun?.id);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin w-10 h-10 border-4 border-[#D2691E] border-t-transparent rounded-full" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-2xl p-6 text-center">
        <p className="text-red-600 font-semibold">{error}</p>
        <button
          onClick={fetchKebun}
          className="mt-4 px-6 py-2 bg-red-500 text-white rounded-xl hover:bg-red-600 transition-colors"
        >
          Coba Lagi
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header & Search */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-[#3D1C00]">Daftar Kebun</h2>
          <p className="text-[#8B6F5A] mt-1">
            {kebunList.length} kebun terdaftar
          </p>
        </div>

        <div className="flex items-center gap-3">
          <input
            type="text"
            placeholder="Cari Nama Kebun..."
            value={searchNama}
            onChange={(e) => setSearchNama(e.target.value)}
            className="px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-900 outline-none focus:ring-2 focus:ring-[#D2691E]"
          />
          <input
            type="text"
            placeholder="Cari Kode..."
            value={searchKode}
            onChange={(e) => setSearchKode(e.target.value)}
            className="px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-900 w-32 outline-none focus:ring-2 focus:ring-[#D2691E]"
          />
          <a
            href="/dashboard/kebun/create"
            className="px-6 py-3 bg-gradient-to-r from-[#D2691E] to-[#8B4513] text-white font-bold rounded-xl shadow-lg hover:shadow-xl hover:scale-[1.02] transition-all flex items-center gap-2"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Tambah Kebun
          </a>
        </div>
      </div>

      {/* Table */}
      {kebunList.length === 0 ? (
        <div className="bg-white rounded-2xl shadow-md p-12 text-center border border-[#EEDDCC]">
          <div className="text-5xl mb-4">🌱</div>
          <h3 className="text-xl font-bold text-[#5D2E0B] mb-2">
            Belum Ada Kebun
          </h3>
          <p className="text-[#8B6F5A] mb-6">
            Mulai dengan menambahkan kebun pertama Anda
          </p>
          <a
            href="/dashboard/kebun/create"
            className="inline-block px-6 py-3 bg-[#D2691E] text-white font-bold rounded-xl hover:bg-[#8B4513] transition-colors"
          >
            Tambah Kebun Pertama
          </a>
        </div>
      ) : (
        <div className="bg-white rounded-2xl shadow-md overflow-hidden border border-[#EEDDCC]">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="bg-gradient-to-r from-[#FDF8F3] to-[#F5EFE7] border-b border-[#EEDDCC]">
                  <th className="px-6 py-4 text-left text-xs font-bold text-[#5D2E0B] uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-bold text-[#5D2E0B] uppercase tracking-wider">
                    Nama Kebun
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-bold text-[#5D2E0B] uppercase tracking-wider">
                    Mandor
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-bold text-[#5D2E0B] uppercase tracking-wider">
                    Supir
                  </th>
                  <th className="px-6 py-4 text-center text-xs font-bold text-[#5D2E0B] uppercase tracking-wider">
                    Aksi
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#F5EFE7]">
                {kebunList.map((kebun) => (
                  <tr
                    key={kebun.id}
                    className="hover:bg-[#FDF8F3]/50 transition-colors"
                  >
                    <td className="px-6 py-4 text-sm font-mono text-[#8B6F5A]">
                      #{kebun.id}
                    </td>
                    <td className="px-6 py-4">
                      <a
                        href={`/dashboard/kebun/${kebun.id}`}
                        className="text-[#3D1C00] font-semibold hover:text-[#D2691E] transition-colors"
                      >
                        {kebun.nama}
                      </a>
                    </td>
                    <td className="px-6 py-4">
                      {kebun.mandorName ? (
                        <span className="inline-flex items-center gap-1 px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-sm font-medium">
                          ✓ {kebun.mandorName}
                        </span>
                      ) : (
                        <span className="text-gray-400 text-sm italic">
                          Belum di-assign
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      {kebun.supirNames && kebun.supirNames.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {kebun.supirNames.map((supir) => (
                            <span
                              key={supir}
                              onClick={() => openModal(kebun, "unassign-supir", supir)}
                              className="inline-flex items-center gap-1 px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm font-medium cursor-pointer hover:bg-blue-100"
                              title="Klik untuk pindahkan supir"
                            >
                              ✓ {supir} 🔄
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-gray-400 text-sm italic">
                          Belum di-assign
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center justify-center gap-2 flex-wrap">
                        {/* Assign/Unassign Mandor */}
                        {kebun.mandorName ? (
                          <button
                            onClick={() => openModal(kebun, "unassign-mandor")}
                            className="px-3 py-1.5 text-xs font-semibold bg-amber-50 text-amber-700 rounded-lg hover:bg-amber-100 transition-colors border border-amber-200"
                          >
                            Pindah Mandor
                          </button>
                        ) : (
                          <button
                            onClick={() => openModal(kebun, "assign-mandor")}
                            className="px-3 py-1.5 text-xs font-semibold bg-emerald-50 text-emerald-700 rounded-lg hover:bg-emerald-100 transition-colors border border-emerald-200"
                          >
                            + Mandor
                          </button>
                        )}

                        {/* Assign Supir */}
                        <button
                          onClick={() => openModal(kebun, "assign-supir")}
                          className="px-3 py-1.5 text-xs font-semibold bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 transition-colors border border-blue-200"
                        >
                          + Supir
                        </button>

                        {/* Edit */}
                        <a
                          href={`/dashboard/kebun/${kebun.id}/edit`}
                          className="px-3 py-1.5 text-xs font-semibold bg-gray-50 text-gray-600 rounded-lg hover:bg-gray-100 transition-colors border border-gray-200"
                        >
                          Edit
                        </a>

                        {/* Delete */}
                        <button
                          onClick={() => handleDelete(kebun.id)}
                          disabled={!!kebun.mandorName}
                          title={kebun.mandorName ? "Tidak bisa dihapus (Mandor terikat)" : "Hapus kebun"}
                          className={`px-3 py-1.5 text-xs font-semibold rounded-lg border transition-colors ${
                            kebun.mandorName
                              ? "bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed"
                              : "bg-red-50 text-red-600 hover:bg-red-100 border-red-200"
                          }`}
                        >
                          Hapus
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal */}
      {modalOpen && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 overflow-hidden">
            <div className="bg-gradient-to-r from-[#8B4513] to-[#D2691E] p-6">
              <h3 className="text-xl font-bold text-white">{getModalTitle()}</h3>
              {selectedKebun && (
                <p className="text-white/70 text-sm mt-1">
                  Kebun: {selectedKebun.nama}
                </p>
              )}
            </div>

            <div className="p-6 space-y-4">
              {/* For ASSIGN: show name input */}
              {!isUnassign && (
                <div className="space-y-1">
                  <label className="text-sm font-bold text-[#5D2E0B] ml-1">
                    {modalType === "assign-mandor" ? "Nama Mandor" : "Nama Supir"}
                  </label>
                  <input
                    type="text"
                    value={inputName}
                    onChange={(e) => setInputName(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-gray-900"
                    placeholder={`Masukkan nama ${modalType === "assign-mandor" ? "mandor" : "supir"}`}
                    autoFocus
                  />
                </div>
              )}

              {/* For UNASSIGN: show who is being moved + target kebun dropdown */}
              {isUnassign && selectedKebun && (
                <>
                  <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 text-sm text-amber-800">
                    ⚠️{" "}
                    <strong>
                      {modalType === "unassign-mandor"
                        ? selectedKebun.mandorName
                        : targetSupir}
                    </strong>{" "}
                    akan dipindahkan ke kebun lain
                  </div>

                  <div className="space-y-1">
                    <label className="text-sm font-bold text-[#5D2E0B] ml-1">
                      Kebun Tujuan
                    </label>
                    <select
                      value={targetKebunId}
                      onChange={(e) => setTargetKebunId(e.target.value)}
                      className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-gray-900"
                    >
                      <option value="">Pilih kebun tujuan...</option>
                      {availableTargets
                        .filter((k) => {
                          // For mandor: only show kebun without a mandor
                          if (modalType === "unassign-mandor") return !k.mandorName;
                          return true;
                        })
                        .map((k) => (
                          <option key={k.id} value={k.id}>
                            {k.nama} ({k.kodeKebun})
                          </option>
                        ))}
                    </select>
                  </div>
                </>
              )}

              <div className="flex gap-3 pt-2">
                <button
                  onClick={() => setModalOpen(false)}
                  className="flex-1 py-3 border-2 border-gray-200 text-gray-600 font-bold rounded-xl hover:bg-gray-50 transition-colors"
                >
                  Batal
                </button>
                <button
                  onClick={handleModalSubmit}
                  disabled={modalLoading || !canSubmit}
                  className={`flex-1 py-3 bg-[#8B4513] text-white font-bold rounded-xl shadow-lg transition-all ${
                    modalLoading || !canSubmit
                      ? "opacity-50 cursor-not-allowed"
                      : "hover:bg-[#703810] active:scale-[0.98]"
                  }`}
                >
                  {modalLoading ? "Memproses..." : "Simpan"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

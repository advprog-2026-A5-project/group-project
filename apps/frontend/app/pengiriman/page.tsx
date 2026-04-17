type ShipmentStatus =
    | "MEMUAT"
    | "MENGIRIM"
    | "TIBA_DI_TUJUAN"
    | "APPROVED_MANDOR"
    | "REJECTED_MANDOR";

type Shipment = {
    id: string;
    tanggal: string;
    tujuan: string;
    beratKg: number;
    status: ShipmentStatus;
    rejectionReason?: string;
};

const activeShipments: Shipment[] = [
    {
        id: "PGM-2026-001",
        tanggal: "2026-04-17",
        tujuan: "Pabrik CPO Utama",
        beratKg: 320,
        status: "MENGIRIM",
    },
    {
        id: "PGM-2026-002",
        tanggal: "2026-04-17",
        tujuan: "Pabrik CPO Timur",
        beratKg: 180,
        status: "MEMUAT",
    },
    {
        id: "PGM-2026-003",
        tanggal: "2026-04-16",
        tujuan: "Pabrik CPO Utama",
        beratKg: 250,
        status: "TIBA_DI_TUJUAN",
    },
];

const historyShipments: Shipment[] = [
    {
        id: "PGM-2026-0008",
        tanggal: "2026-04-14",
        tujuan: "Pabrik CPO Barat",
        beratKg: 300,
        status: "APPROVED_MANDOR",
    },
    {
        id: "PGM-2026-0007",
        tanggal: "2026-04-12",
        tujuan: "Pabrik CPO Utama",
        beratKg: 290,
        status: "REJECTED_MANDOR",
        rejectionReason: "Dokumen timbangan tidak lengkap",
    },
];

function statusBadgeClass(status: ShipmentStatus): string {
    switch (status) {
        case "MEMUAT":
            return "bg-amber-100 text-amber-700 border border-amber-300";
        case "MENGIRIM":
            return "bg-blue-100 text-blue-700 border border-blue-300";
        case "TIBA_DI_TUJUAN":
            return "bg-purple-100 text-purple-700 border border-purple-300";
        case "APPROVED_MANDOR":
            return "bg-green-100 text-green-700 border border-green-300";
        case "REJECTED_MANDOR":
            return "bg-red-100 text-red-700 border border-red-300";
        default:
            return "bg-gray-100 text-gray-700 border border-gray-300";
    }
}

export default function PengirimanPage() {
    const totalAktif = activeShipments.length;
    const totalMengirim = activeShipments.filter((shipment) => shipment.status === "MENGIRIM").length;
    const totalMenungguMandor = activeShipments.filter(
        (shipment) => shipment.status === "TIBA_DI_TUJUAN"
    ).length;

    return (
        <div className="min-h-screen bg-[#FDF8F3]">
            <div className="h-2 w-full bg-[#8B4513]" />

            <main className="mx-auto w-full max-w-6xl px-4 py-8 md:px-8">
                <header className="mb-8 flex flex-col gap-4 rounded-2xl border border-[#EEDDCC] bg-white p-6 shadow-lg md:flex-row md:items-center md:justify-between">
                    <div>
                        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#A56A3A]">
                            Dashboard Supir
                        </p>
                        <h1 className="mt-2 text-3xl font-extrabold text-[#5D2E0B]">Pengiriman Saya</h1>
                        <p className="mt-2 text-sm text-[#7B5B46]">
                            Tampilan sederhana untuk memantau penugasan, status kirim, dan riwayat pengiriman.
                        </p>
                    </div>

                    <div className="rounded-xl border border-[#F1D8BE] bg-[#FFF7ED] px-4 py-3 text-sm text-[#7B5B46]">
                        <p className="font-bold text-[#8B4513]">Supir: driver.budi (Placeholder)</p>
                        <p>ID: DRV-0002 (Placeholder)</p>
                        <p>Kebun: Kebun Cisarua (Placeholder)</p>
                    </div>
                </header>

                <section className="mb-8 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-sm text-amber-800 shadow-sm">
                    <p className="font-bold">Placeholder UI</p>
                    <p className="mt-1">
                        Seluruh angka, daftar pengiriman, dan identitas di halaman ini masih contoh sementara.
                        Nanti akan diganti data API backend Pengiriman.
                    </p>
                </section>

                <section className="mb-8 grid grid-cols-1 gap-4 md:grid-cols-3">
                    <div className="rounded-2xl border border-[#EEDDCC] bg-white p-5 shadow-sm">
                        <p className="text-xs font-semibold uppercase tracking-wide text-[#A56A3A]">Total Aktif (Placeholder)</p>
                        <p className="mt-2 text-3xl font-extrabold text-[#5D2E0B]">{totalAktif}</p>
                        <p className="mt-1 text-sm text-[#7B5B46]">Pengiriman berjalan hari ini</p>
                    </div>

                    <div className="rounded-2xl border border-[#EEDDCC] bg-white p-5 shadow-sm">
                        <p className="text-xs font-semibold uppercase tracking-wide text-[#A56A3A]">Dalam Perjalanan (Placeholder)</p>
                        <p className="mt-2 text-3xl font-extrabold text-[#5D2E0B]">{totalMengirim}</p>
                        <p className="mt-1 text-sm text-[#7B5B46]">Truk sedang menuju tujuan</p>
                    </div>

                    <div className="rounded-2xl border border-[#EEDDCC] bg-white p-5 shadow-sm">
                        <p className="text-xs font-semibold uppercase tracking-wide text-[#A56A3A]">Menunggu Approval (Placeholder)</p>
                        <p className="mt-2 text-3xl font-extrabold text-[#5D2E0B]">{totalMenungguMandor}</p>
                        <p className="mt-1 text-sm text-[#7B5B46]">Sudah tiba di tujuan</p>
                    </div>
                </section>

                <section className="mb-8 rounded-2xl border border-[#EEDDCC] bg-white p-6 shadow-lg">
                    <div className="mb-5 flex items-center justify-between">
                        <h2 className="text-xl font-bold text-[#5D2E0B]">Daftar Pengiriman Aktif (Placeholder)</h2>
                        <button className="rounded-xl bg-[#8B4513] px-4 py-2 text-sm font-bold text-white hover:bg-[#703810]">
                            Refresh Data Contoh
                        </button>
                    </div>

                    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                        {activeShipments.map((shipment) => (
                            <article
                                key={shipment.id}
                                className="rounded-xl border border-[#F1D8BE] bg-[#FFFCF8] p-4"
                            >
                                <div className="mb-3 flex items-start justify-between gap-3">
                                    <p className="text-sm font-extrabold text-[#5D2E0B]">{shipment.id}</p>
                                    <span
                                        className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusBadgeClass(
                                            shipment.status
                                        )}`}
                                    >
                                        {shipment.status}
                                    </span>
                                </div>

                                <dl className="space-y-1.5 text-sm text-[#7B5B46]">
                                    <div className="flex justify-between gap-2">
                                        <dt>Tanggal</dt>
                                        <dd className="font-semibold">{shipment.tanggal}</dd>
                                    </div>
                                    <div className="flex justify-between gap-2">
                                        <dt>Tujuan</dt>
                                        <dd className="font-semibold">{shipment.tujuan}</dd>
                                    </div>
                                    <div className="flex justify-between gap-2">
                                        <dt>Berat</dt>
                                        <dd className="font-semibold">{shipment.beratKg} Kg</dd>
                                    </div>
                                </dl>
                            </article>
                        ))}
                    </div>
                </section>

                <section className="rounded-2xl border border-[#EEDDCC] bg-white p-6 shadow-lg">
                    <h2 className="mb-4 text-xl font-bold text-[#5D2E0B]">Riwayat Pengiriman (Placeholder)</h2>

                    <div className="space-y-3">
                        {historyShipments.map((shipment) => (
                            <article
                                key={shipment.id}
                                className="rounded-xl border border-[#F1D8BE] bg-[#FFFCF8] p-4"
                            >
                                <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                                    <div>
                                        <p className="font-extrabold text-[#5D2E0B]">{shipment.id}</p>
                                        <p className="text-sm text-[#7B5B46]">
                                            {shipment.tanggal} • {shipment.tujuan} • {shipment.beratKg} Kg
                                        </p>
                                    </div>
                                    <span
                                        className={`w-fit rounded-full px-2.5 py-1 text-xs font-bold ${statusBadgeClass(
                                            shipment.status
                                        )}`}
                                    >
                                        {shipment.status}
                                    </span>
                                </div>

                                {shipment.rejectionReason && (
                                    <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
                                        Alasan penolakan: {shipment.rejectionReason}
                                    </p>
                                )}
                            </article>
                        ))}
                    </div>
                </section>
            </main>
        </div>
    );
}

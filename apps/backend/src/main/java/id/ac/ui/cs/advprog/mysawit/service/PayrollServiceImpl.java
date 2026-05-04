package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import id.ac.ui.cs.advprog.mysawit.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.model.Upah;
import id.ac.ui.cs.advprog.mysawit.repository.PayrollRepository;
import id.ac.ui.cs.advprog.mysawit.repository.UpahRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollServiceImpl implements PayrollService {

    private static final double PAYROLL_FACTOR = 0.9;

    private final PayrollRepository payrollRepository;
    private final UpahRepository upahRepository;

    public PayrollServiceImpl(PayrollRepository payrollRepository, UpahRepository upahRepository) {
        this.payrollRepository = payrollRepository;
        this.upahRepository = upahRepository;
    }

    @Override
    @Transactional
    public Payroll create(Payroll payroll) {
        return payrollRepository.save(payroll);
    }

    @Override
    @Transactional
    public Payroll createWithKilogram(Long userId, UpahRole role, double kilogram) {
        validateUser(userId);
        validateRole(role);
        validateKilogram(kilogram);
        Upah upah = upahRepository.findByRole(role)
                .orElseThrow(() -> new IllegalArgumentException("Upah untuk role tidak ditemukan"));
        double amount = upah.getUpahPerKg() * kilogram * PAYROLL_FACTOR;
        Payroll payroll = new Payroll();
        payroll.setUserId(userId);
        payroll.setAmount(amount);
        return payrollRepository.save(payroll);
    }

    @Override
    public Payroll getById(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll tidak ditemukan"));
    }

    @Override
    public List<Payroll> getAll() {
        return payrollRepository.findAll();
    }

    @Override
    @Transactional
    public Payroll update(Payroll payroll) {
        if (payroll.getId() == null || !payrollRepository.existsById(payroll.getId())) {
            throw new IllegalArgumentException("Payroll tidak ditemukan");
        }
        return payrollRepository.save(payroll);
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId tidak boleh kosong");
        }
    }

    private void validateRole(UpahRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role tidak boleh kosong");
        }
    }

    private void validateKilogram(double kilogram) {
        if (kilogram <= 0) {
            throw new IllegalArgumentException("Kilogram harus lebih dari 0");
        }
    }
}

package id.ac.ui.cs.advprog.mysawit.service;

import id.ac.ui.cs.advprog.mysawit.model.Payroll;
import id.ac.ui.cs.advprog.mysawit.enums.UpahRole;
import java.util.List;

public interface PayrollService {
    Payroll create(Payroll payroll);
    Payroll createWithKilogram(Long userId, UpahRole role, double kilogram);
    Payroll getById(Long id);
    List<Payroll> getAll();
    Payroll update(Payroll payroll);
}

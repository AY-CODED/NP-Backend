package org.admin.npapplication.repository;

import org.admin.npapplication.model.PrescriptionDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionDocumentRepository extends JpaRepository<PrescriptionDocument, Long> {
}

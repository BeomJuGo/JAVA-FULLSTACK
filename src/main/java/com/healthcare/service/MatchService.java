package com.healthcare.service;

import com.healthcare.domain.Match;
import com.healthcare.dto.match.MatchDtos;
import com.healthcare.repository.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository repo;

    public MatchService(MatchRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Match createRequest(MatchDtos.RequestCreate req) {
        var m = new Match();
        m.setUserId(req.userId);
        m.setTrainerId(req.trainerId);
        m.setRequestedBy(Match.RequestedBy.valueOf(req.requestedBy.toUpperCase()));
        m.setStatus(Match.Status.REQUESTED);
        m.setRequestedAt(LocalDateTime.now());
        return repo.save(m);
    }

    @Transactional
    public Match accept(Long matchId) {
        var m = repo.findById(matchId).orElseThrow();
        if (m.getStatus() != Match.Status.REQUESTED) {
            throw new IllegalStateException("match is not REQUESTED");
        }
        m.setStatus(Match.Status.ACCEPTED);
        m.setAcceptedAt(LocalDateTime.now());
        return m;
    }

    @Transactional
    public Match start(Long matchId) {
        var m = repo.findById(matchId).orElseThrow();
        if (m.getStatus() != Match.Status.ACCEPTED) {
            throw new IllegalStateException("match is not ACCEPTED");
        }
        m.setStatus(Match.Status.IN_PROGRESS);
        return m;
    }

    @Transactional
    public Match end(Long matchId, String reason) {
        var m = repo.findById(matchId).orElseThrow();
        if (m.getStatus() != Match.Status.IN_PROGRESS && m.getStatus() != Match.Status.ACCEPTED) {
            throw new IllegalStateException("match is not ACTIVE");
        }
        m.setStatus(Match.Status.ENDED);
        m.setEndedAt(LocalDateTime.now());
        m.setEndReason(reason);
        return m;
    }

    @Transactional
    public Match reject(Long matchId, String reason) {
        var m = repo.findById(matchId).orElseThrow();
        if (m.getStatus() != Match.Status.REQUESTED) {
            throw new IllegalStateException("match is not REQUESTED");
        }
        m.setStatus(Match.Status.REJECTED);
        m.setEndReason(reason);
        m.setEndedAt(LocalDateTime.now());
        return m;
    }

    @Transactional
    public Match forceEnd(Long matchId, String reason) {
        var m = repo.findById(matchId).orElseThrow();
        if (m.getStatus() == Match.Status.ENDED || m.getStatus() == Match.Status.REJECTED) {
            throw new IllegalStateException("match already closed");
        }
        m.setStatus(Match.Status.FORCE_ENDED);
        m.setEndReason(reason);
        m.setEndedAt(LocalDateTime.now());
        return m;
    }

    @Transactional
    public Match block(Long matchId, String reason) {
        var m = repo.findById(matchId).orElseThrow();
        m.setBlocked(true);
        m.setBlockReason(reason);
        return m;
    }

    @Transactional
    public Match report(Long matchId, String reason) {
        var m = repo.findById(matchId).orElseThrow();
        m.setReported(true);
        m.setReportReason(reason);
        return m;
    }

    @Transactional(readOnly = true)
    public Match get(Long id) { return repo.findById(id).orElseThrow(); }

    @Transactional(readOnly = true)
    public List<Match> findByUserId(Long userId) {
        return repo.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Match> findByTrainerId(Long trainerId) {
        return repo.findByTrainerId(trainerId);
    }

    @Transactional(readOnly = true)
    public List<Match> findByUserIdAndStatus(Long userId, Match.Status status) {
        return repo.findByUserIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public List<Match> findByTrainerIdAndStatus(Long trainerId, Match.Status status) {
        return repo.findByTrainerIdAndStatus(trainerId, status);
    }
}

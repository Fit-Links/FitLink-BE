package spring.fitlinkbe.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final WorkoutScheduleRepository workoutScheduleRepository;

    public Member saveMember(Member member) {
        return memberRepository.saveMember(member).orElseThrow();
    }

    public void saveWorkoutSchedules(List<WorkoutSchedule> workoutSchedules) {
        workoutScheduleRepository.saveAll(workoutSchedules);
    }
}

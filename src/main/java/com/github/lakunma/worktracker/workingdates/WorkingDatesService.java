package com.github.lakunma.worktracker.workingdates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
public class WorkingDatesService {

    private final DayTypeRepository dayTypeRepository;
    private final Set<LocalDate> workingOnWeekendDays = new HashSet<>();
    private final Set<LocalDate> holidayDays = new HashSet<>();

    @Autowired
    public WorkingDatesService(DayTypeRepository dayTypeRepository) {
        this.dayTypeRepository = dayTypeRepository;
        for (DayType dayType : dayTypeRepository.findAll()) {
            if (dayType.isWorkDayOnHoliday()) {
                workingOnWeekendDays.add(dayType.getDate());
            } else {
                holidayDays.add(dayType.getDate());
            }

        }
    }

    public boolean isWorking(LocalDate date) {
        if (holidayDays.contains(date)) {
            return false;
        }
        if (workingOnWeekendDays.contains(date)) {
            return true;
        }
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    public List<LocalDate> workingDaysTillNow(int nDays) {
        ArrayList<LocalDate> workingDays = new ArrayList<>();

        int remainingDays = nDays;
        LocalDate curDay = LocalDate.now();
        while (remainingDays > 0) {
            if (isWorking(curDay)) {
                workingDays.add(curDay);
                remainingDays--;
            }
            curDay = curDay.minusDays(1);
        }

        Collections.reverse(workingDays);
        return workingDays;
    }

    public LocalDate workingDayBefore(int nDays) {
        return workingDaysTillNow(nDays).get(0);
    }

    public int workingDayFromNow(LocalDate date) {
        int workingDays = 0;
        for (LocalDate curDay = LocalDate.now(); !curDay.isBefore(date); curDay = curDay.minusDays(1)) {
            if (isWorking(curDay)) {
                workingDays++;
            }
        }
        return workingDays - 1;
    }
}

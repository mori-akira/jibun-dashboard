package main

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"os"
	"time"

	"github.com/google/uuid"
)

type PayslipDetail struct {
	Key  string `json:"key"`
	Data int    `json:"data"`
}

type PayslipCategory struct {
	Key  string          `json:"key"`
	Data []PayslipDetail `json:"data"`
}

type Overview struct {
	GrossIncome   int `json:"grossIncome"`
	NetIncome     int `json:"netIncome"`
	OperatingTime int `json:"operatingTime"`
	Overtime      int `json:"overtime"`
	Bonus         int `json:"bonus"`
	BonusTakeHome int `json:"bonusTakeHome"`
}

type Structure struct {
	BasicSalary       int `json:"basicSalary"`
	OvertimePay       int `json:"overtimePay"`
	HousingAllowance  int `json:"housingAllowance"`
	PositionAllowance int `json:"positionAllowance"`
	Other             int `json:"other"`
}

type Salary struct {
	SalaryId   string            `json:"salaryId"`
	UserId     string            `json:"userId"`
	TargetDate string            `json:"targetDate"`
	Overview   Overview          `json:"overview"`
	Structure  Structure         `json:"structure"`
	Payslip    []PayslipCategory `json:"payslipData"`
}

const (
	startYear         = 2015
	endYear           = 2025
	baseSalary        = 200000
	raisePerYear      = 10000
	baseOvertimeRate  = 2000
	overtimeRaiseYear = 100
	housingAllowance  = 30000
	otherFixed        = 5000
	maxPaidLeaveYear  = 15

	socialInsuranceRate = 0.15
	healthInsuranceRate = 0.05
	incomeTaxRate       = 0.06
	residentTaxRate     = 0.03

	standardDailyHours = 8
)

var workdaysPerMonth = []int{18, 18, 21, 20, 19, 22, 19, 18, 20, 21, 20, 19}

func main() {
	rand.NewSource(time.Now().UnixNano())
	userId := "test-user"
	var results []Salary

	paidLeaveCount := 0

	for year := startYear; year <= endYear; year++ {
		for month := 1; month <= 12; month++ {
			if year == endYear && month > 3 {
				break
			}

			date := fmt.Sprintf("%04d-%02d-01", year, month)
			salaryUUID := uuid.New().String()

			salary := baseSalary + (year-startYear)*raisePerYear
			overtimeRate := baseOvertimeRate + (year-startYear)*overtimeRaiseYear
			workdays := workdaysPerMonth[month-1]
			paidLeave := rand.Intn(4)
			if paidLeaveCount+paidLeave > maxPaidLeaveYear {
				paidLeave = 0
			}
			paidLeaveCount += paidLeave

			otHours := float64(rand.Intn(9)) * 0.5 * float64(workdays) // 0~4時間を0.5刻み、月全体
			overtime := int(otHours)
			overtimePay := int(float64(overtime) * float64(overtimeRate))

			bonus := 0
			bonusTakeHome := 0
			if month == 6 || month == 12 {
				multiplier := 2.5 + float64(rand.Intn(11))/10.0 // 2.5 ~ 3.5
				bonus = int(multiplier * float64(salary))
				bonusTakeHome = int(float64(bonus) * (1.0 - (socialInsuranceRate + healthInsuranceRate + incomeTaxRate + residentTaxRate)))
			}

			gross := salary + overtimePay + housingAllowance + otherFixed
			deductions := int(float64(gross) * (socialInsuranceRate + healthInsuranceRate + incomeTaxRate + residentTaxRate))
			net := gross - deductions

			results = append(results, Salary{
				SalaryId:   salaryUUID,
				UserId:     userId,
				TargetDate: date,
				Overview: Overview{
					GrossIncome:   gross,
					NetIncome:     net,
					OperatingTime: workdays*standardDailyHours + overtime,
					Overtime:      overtime,
					Bonus:         bonus,
					BonusTakeHome: bonusTakeHome,
				},
				Structure: Structure{
					BasicSalary:       salary,
					OvertimePay:       overtimePay,
					HousingAllowance:  housingAllowance,
					PositionAllowance: 0,
					Other:             otherFixed,
				},
				Payslip: []PayslipCategory{
					{
						Key: "勤怠",
						Data: []PayslipDetail{
							{"稼働時間", workdays * standardDailyHours},
							{"残業時間", overtime},
							{"有給取得", paidLeave},
						},
					},
					{
						Key: "支給",
						Data: []PayslipDetail{
							{"基本給", salary},
							{"残業代", overtimePay},
							{"住宅手当", housingAllowance},
							{"その他", otherFixed},
							{"ボーナス", bonus},
						},
					},
					{
						Key: "控除",
						Data: []PayslipDetail{
							{"社会保険料", int(float64(gross) * socialInsuranceRate)},
							{"健康保険料", int(float64(gross) * healthInsuranceRate)},
							{"所得税", int(float64(gross) * incomeTaxRate)},
							{"住民税", int(float64(gross) * residentTaxRate)},
						},
					},
				},
			})
		}
	}

	out1, _ := json.MarshalIndent(results, "", "  ")
	os.WriteFile("../../openapi/examples/salary/get.json", out1, 0644)
	os.WriteFile("../../front/public/mock-api/salary.json", out1, 0644)

	out2, _ := json.MarshalIndent(results[len(results)-1], "", "  ")
	os.WriteFile("../../openapi/examples/salary/salaryId/get.json", out2, 0644)
	os.WriteFile(fmt.Sprintf("../../front/public/mock-api/salary/%s.json", results[len(results)-1].SalaryId), out2, 0644)
}

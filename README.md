# CalendarView


 mCalendarView.setOnClickDate(new CalendarView.OnClickListener() {
            
            @Override
            public void onClickDateListener(int year, int month, int day) {
                Toast.makeText(getApplication(), year + "-" + month + "-" + day , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClickNextMonthListener() {
                Toast.makeText(MainActivity.this, "下个月", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClickLastMonthListener() {
                Toast.makeText(MainActivity.this, "上个月", Toast.LENGTH_SHORT).show();
            }
        });


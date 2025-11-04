package com.multifinance.ui;

import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.multifinance.R;
public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getBottomNavItemId();

    /**
     * Настраивает общую шапку — логотип и кнопку уведомлений.
     * Вызывается в onCreate() дочерних Activity.
     */
    protected void setupHeader() {
        ImageButton btnNotifications = findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    Toast.makeText(this, "Уведомления пока недоступны", Toast.LENGTH_SHORT).show()
            );
        }
    }

    /**
     * Настраивает нижнюю панель навигации.
     * Вызывать после setContentView().
     */
    protected void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav == null) return;

        int currentItemId = getBottomNavItemId();
        bottomNav.setSelectedItemId(currentItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentItemId) return true;

            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else if (itemId == R.id.nav_accounts) {
                startActivity(new Intent(this, AccountsActivity.class));
            } else if (itemId == R.id.nav_analytics) {
                Toast.makeText(this, "Раздел 'Аналитика' в разработке", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_profile) {
                Toast.makeText(this, "Раздел 'Профиль' в разработке", Toast.LENGTH_SHORT).show();
            }

            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }

}

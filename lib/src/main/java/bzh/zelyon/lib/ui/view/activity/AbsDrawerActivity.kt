package bzh.zelyon.lib.ui.view.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import bzh.zelyon.lib.R
import bzh.zelyon.lib.extension.*
import bzh.zelyon.lib.ui.view.fragment.AbsFragment
import com.google.android.material.navigation.NavigationView

abstract class AbsDrawerActivity: AbsActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var navigationItems = listOf<NavigationItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getNavigationView().inflateMenu(getNavigationMenuId())
        getNavigationView().setNavigationItemSelectedListener(this)
        navigationItems = getNavigationItems()
        onNavigationItemSelected(getNavigationView().menu.findItem(getSelectedNavigationItemId()))
    }

    override fun onBackPressed() {
        if (getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        navigationItems.find { it.id == item.itemId }?.let { navigationItem ->
            item.isChecked = navigationItem.isRoot
            fullBack()
            showFragment(navigationItem.fragment, !navigationItem.isRoot)
        }
        closeDrawer()
        return true
    }

    abstract fun getDrawerLayout(): DrawerLayout

    abstract fun getNavigationView(): NavigationView

    abstract fun getSelectedNavigationItemId(): Int

    open fun getNavigationMenuId() = R.menu.empty

    open fun getNavigationItems() = listOf<NavigationItem>()

    fun openDrawer() = getDrawerLayout().openDrawer(GravityCompat.START)

    fun closeDrawer() = getDrawerLayout().closeDrawer(GravityCompat.START)

    class NavigationItem(val id: Int, val fragment: AbsFragment, val isRoot: Boolean = true)
}
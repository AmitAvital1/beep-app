package beep.app.login;

import java.util.ArrayList;
import java.util.List;

import beep.app.R;

public interface Regions {
    List<RegionItem> ALL_REGIONS = createRegionItems();

    static List<RegionItem> createRegionItems() {
        List<RegionItem> regionItems = new ArrayList<>();
        regionItems.add(new RegionItem("+1", 1, R.drawable.flag_usa));
        regionItems.add(new RegionItem("+44", 44, R.drawable.flag_uk));
        regionItems.add(new RegionItem("+972", 972, R.drawable.flag_israel));
        return regionItems;
    }
}

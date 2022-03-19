package megamek.test;

import megamek.common.EquipmentType;
import megamek.common.WeaponType;
import megamek.common.alphaStrike.AlphaStrikeElement;
import megamek.common.weapons.bayweapons.BayWeapon;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ASWeaponDamageList {
    
    public static void main(String[] args) {
        List<String> wpLine;
        for (Enumeration<EquipmentType> e = EquipmentType.getAllTypes(); e.hasMoreElements(); ) {
            EquipmentType etype = e.nextElement();
            if (etype instanceof WeaponType && !((WeaponType)etype).rulesRefs.equals("Unofficial")
                    &&!(etype instanceof BayWeapon)) {
                wpLine = new ArrayList<>();
                wpLine.add(etype.getName());
                wpLine.add(etype.isClan()? "-Clan-" : "-IS-");
                double mult = ((WeaponType)etype).hasFlag(WeaponType.F_ONESHOT) ? 0.1 : 1;
                double s = mult * ((WeaponType)etype).getBattleForceDamage(AlphaStrikeElement.SHORT_RANGE, null);
                String sT = s == 0 ? "--" : "" + s;
                double m = mult * ((WeaponType)etype).getBattleForceDamage(AlphaStrikeElement.MEDIUM_RANGE, null);
                String mT = m == 0 ? "--" : "" + m;
                double l = mult * ((WeaponType)etype).getBattleForceDamage(AlphaStrikeElement.LONG_RANGE, null);
                String lT = l == 0 ? "--" : "" + l;
                double ex = mult * ((WeaponType)etype).getBattleForceDamage(AlphaStrikeElement.EXTREME_RANGE, null);
                String exT = ex == 0 ? "--" : "" + ex;
                wpLine.add(sT);
                wpLine.add(mT);
                wpLine.add(lT);
                wpLine.add(exT);
                
                System.out.println(String.join("\t", wpLine));
            }
        }
        
    }

}


public interface IFamilyTable {

    boolean getIsExtendable();
    void setIsExtendable(boolean val);
    boolean getHasKidChair();
    void setHasKidChair(boolean val);
    double calculateKidDiscount(int numOfKids);
}

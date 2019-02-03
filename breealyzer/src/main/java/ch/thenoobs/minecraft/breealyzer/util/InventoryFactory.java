package ch.thenoobs.minecraft.breealyzer.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

public class InventoryFactory {
	private static HashMap<String, Class<? extends InventoryHandlerEntityPair>> typeMappings = new HashMap<String, Class<? extends InventoryHandlerEntityPair>>();
	
	public static <T extends InventoryHandlerEntityPair> T GetInvenotryHandler(TileEntity tileEntity, IItemHandler itemHandler) {				
		T inventoryHandler = null;

		String typeName = tileEntity.getClass().getName();
				
		Class<? extends InventoryHandlerEntityPair> handlerType = typeMappings.get(typeName);		
		
		if (handlerType != null) {			
			try {
				Constructor<?> constructor = handlerType.getConstructor(TileEntity.class, IItemHandler.class);
				
				inventoryHandler = (T)constructor.newInstance(tileEntity, itemHandler);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {

				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			inventoryHandler = (T) new InventoryHandlerEntityPair(tileEntity, itemHandler);
		}
				
		return inventoryHandler;
	}
	
	public static Boolean registerInventoryHander(String typeName, Class<? extends InventoryHandlerEntityPair> handlerType) {
		Boolean isSuccessful = false;
		
		if ((typeName != null) && (!typeName.isEmpty())) {
			if (!typeMappings.containsKey(typeName)) {
				typeMappings.put(typeName, handlerType);
				isSuccessful = true;
			}
		}
		
		return isSuccessful;
	}
	
	public static List<Class<? extends InventoryHandlerEntityPair>> getRegisteredHandlers(){
		return typeMappings.values().stream().collect(Collectors.toList());
	}
}

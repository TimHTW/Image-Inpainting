clear;
clc;
pkg load image;

Image = 'norway_java.png';
gif_file = filename = "gif_without_LaplaceFilter_without_border.gif";
I = double(imread(Image));

v = 1; %Viskosität
t = 0;
tau = 0.01;
tau2 = 0.01;
alpha = 10;
counter = 0;
row = rows(I);

I_red = I(:,:,1);
I_green = I(:,:,2);
I_blue = I(:,:,3);
M =  (I(:,:,1) >= 253) & (I(:,:,2) == 0) & (I(:,:,3) == 0);

index = find(M==1); #index only for painted area
M(index-1) = 1;
M(index+1) = 1;
M(index-row) = 1;
M(index+row) = 1;
M(index) = 0;
index2 = find(M==1); %index only for border (pixel next to painted area)


%first value for vorticity w
W_red(rows(I),columns(I)) = 0;
W_green(rows(I),columns(I)) = 0;
W_blue(rows(I),columns(I)) = 0;

W_red(2:end-1,2:end-1) = (I(1:end-2,2:end-1,1)-2*I(2:end-1,2:end-1,1)+I(3:end,2:end-1,1)) + (I(2:end-1,1:end-2,1)-2*I(2:end-1,2:end-1,1)+I(2:end-1,3:end,1));
W_green(2:end-1,2:end-1) = (I(1:end-2,2:end-1,2)-2*I(2:end-1,2:end-1,2)+I(3:end,2:end-1,2)) + (I(2:end-1,1:end-2,2)-2*I(2:end-1,2:end-1,2)+I(2:end-1,3:end,2));
W_blue(2:end-1,2:end-1) = (I(1:end-2,2:end-1,3)-2*I(2:end-1,2:end-1,3)+I(3:end,2:end-1,3)) + (I(2:end-1,1:end-2,3)-2*I(2:end-1,2:end-1,3)+I(2:end-1,3:end,3));

while (t <= 500)
  
  
  l(:,:,1) = I_red;
  l(:,:,2) = I_green;
  l(:,:,3) = I_blue;
  if t == 0
      imwrite(uint8(l), gif_file, 'gif', 'LoopCount', Inf, 'DelayTime', 0.5);
  elseif mod(counter,100) == 0
      imwrite(uint8(l), gif_file, 'gif', 'WriteMode', 'append', 'DelayTime', 0.5);
  endif
  #if mod(counter,50) == 0
  
  I_red_old = I_red(index);
  I_green_old = I_green(index);
  I_blue_old = I_blue(index);
  
  %Upwind-Start
  Ix_red(rows(I),columns(I)) = 0;
  Iy_red = Ix_green = Iy_green = Ix_blue = Iy_blue = Ix_red;
  Ix_rgb(rows(I),columns(I)) = 0;
  Iy_rgb(rows(I),columns(I)) = 0;

  Iy_red(index) = (I_red(index-1)-I_red(index+1))./2;
  Ix_red(index) = -(I_red(index+row)-I_red(index-row))./2;
  Iy_green(index) = (I_green(index-1)-I_green(index+1))./2;
  Ix_green(index) = -(I_green(index+row)-I_green(index-row))./2;
  Iy_blue(index) = (I_blue(index-1)-I_blue(index+1))./2;
  Ix_blue(index) = -(I_blue(index+row)-I_blue(index-row))./2;
  

  Ix_rgb(index) = (Ix_red(index) + Ix_green(index) + Ix_blue(index)) ./ 3;
  Iy_rgb(index) = (Iy_red(index) + Iy_green(index) + Iy_blue(index)) ./ 3;
  
  index3_rgb = (Iy_rgb > 0);
  index4_rgb = (Iy_rgb < 0);
  index5_rgb = (Ix_rgb > 0);
  index6_rgb = (Ix_rgb < 0);

  
  %First-Order - Upwind => Stabilität
  scalar_red = index5_rgb(index).*Ix_rgb(index) .* ((W_red(index)-W_red(index+1))./ 2)...
               +index6_rgb(index).*Ix_rgb(index) .* ((W_red(index-1)-W_red(index))./ 2)...
               +index3_rgb(index).*Iy_rgb(index) .* ((W_red(index)-W_red(index-row))./ 2)...
               +index4_rgb(index).*Iy_rgb(index) .* ((W_red(index+row)-W_red(index))./ 2);
  
  scalar_green = index5_rgb(index).*Ix_rgb(index) .* ((W_green(index)-W_green(index+1))./ 2)...
                 +index6_rgb(index).*Ix_rgb(index) .* ((W_green(index-1)-W_green(index))./ 2)...
                 +index3_rgb(index).*Iy_rgb(index) .* ((W_green(index)-W_green(index-row))./ 2)...
                 +index4_rgb(index).*Iy_rgb(index) .* ((W_green(index+row)-W_green(index))./ 2);
  
  scalar_blue  = index5_rgb(index).*Ix_rgb(index) .* ((W_blue(index)-W_blue(index+1))./ 2)...
                 +index6_rgb(index).*Ix_rgb(index) .* ((W_blue(index-1)-W_blue(index))./ 2)...
                 +index3_rgb(index).*Iy_rgb(index) .* ((W_blue(index)-W_blue(index-row))./ 2)...
                 +index4_rgb(index).*Iy_rgb(index) .* ((W_blue(index+row)-W_blue(index))./ 2);
  %Upwind-End

  LaplaceW_red   = v * (W_red(index-1)-4*W_red(index)+W_red(index+1) + W_red(index-row)+W_red(index+row));
  LaplaceW_green = v * ((W_green(index-1)-2*W_green(index)+W_green(index+1)) + (W_green(index-row)-2*W_green(index)+W_green(index+row)));
  LaplaceW_blue  = v * ((W_blue(index-1)-2*W_blue(index)+W_blue(index+1)) + (W_blue(index-row)-2*W_blue(index)+W_blue(index+row)));
  
  W_red(index)   = tau * (LaplaceW_red-scalar_red) + W_red(index);
  W_green(index) = tau * (LaplaceW_green-scalar_green) + W_green(index);
  W_blue(index)  = tau * (LaplaceW_blue-scalar_blue) + W_blue(index);
  
  if(counter == 0)
    W_red(index)
  endif

  z = 0;
  while z <= 500
    L_red_old   = I_red(index);
    L_green_old = I_green(index);
    L_blue_old  = I_blue(index);
    
    LaplaceI_red = (I_red(index-1)-2*I_red(index)+I_red(index+1)) + (I_red(index-row)-2*I_red(index)+I_red(index+row));
    LaplaceI_green = (I_green(index-1)-2*I_green(index)+I_green(index+1)) + (I_green(index-row)-2*I_green(index)+I_green(index+row));
    LaplaceI_blue = (I_blue(index-1)-2*I_blue(index)+I_blue(index+1)) + (I_blue(index-row)-2*I_blue(index)+I_blue(index+row));
    
    I_red(index) = tau2 * (alpha * (LaplaceI_red - W_red(index))) + L_red_old;
    I_green(index) = tau2 * (alpha * (LaplaceI_green - W_green(index))) + L_green_old;
    I_blue(index) = tau2 * (alpha * (LaplaceI_blue - W_blue(index))) + L_blue_old;
    
    L_red_new = I_red(index);
    L_green_new = I_green(index);
    L_blue_new = I_blue(index);
    
    if (abs(L_red_old - L_red_new) <= 0.01 && abs(L_green_old - L_green_new) <= 0.01 && abs(L_blue_old - L_blue_new) <= 0.01)
      break;
    endif
    z = z + 1;
  endwhile
  
  I_red_new = I_red(index);
  I_green_new = I_green(index);
  I_blue_new = I_blue(index);
  
   if (abs(I_red_old - I_red_new) <= 0.01 && abs(I_green_old - I_green_new) <= 0.01 && abs(I_blue_old - I_blue_new ) <= 0.01)
    break;
   endif
   
   W_red(index2)   = (I_red(index2-1)-2*I_red(index2)+I_red(index2+1)) + (I_red(index2-row)-2*I_red(index2)+I_red(index2+row));
   W_green(index2) = (I_green(index2-1)-2*I_green(index2)+I_green(index2+1)) + (I_green(index2-row)-2*I_green(index2)+I_green(index2+row));
   W_blue(index2)  = (I_blue(index2-1)-2*I_blue(index2)+I_blue(index2+1)) + (I_blue(index2-row)-2*I_blue(index2)+I_blue(index2+row));
   
   t = t + tau;
   counter++;
endwhile

I(:,:,1) = I_red;
I(:,:,2) = I_green;
I(:,:,3) = I_blue;

result_img = sprintf('%s.jpg', 'result_image');
imwrite(uint8(I), result_img);